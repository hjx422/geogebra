package org.geogebra.common.exam;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.geogebra.common.exam.restrictions.ExamRestrictable;
import org.geogebra.common.exam.restrictions.ExamRestrictions;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.CommandDispatcher;
import org.geogebra.common.main.exam.TempStorage;
import org.geogebra.common.main.exam.event.CheatingEvents;
import org.geogebra.common.move.ggtapi.models.Material;
import org.geogebra.common.ownership.NonOwning;
import org.geogebra.common.properties.PropertiesRegistry;
import org.geogebra.common.properties.PropertiesRegistryListener;
import org.geogebra.common.properties.Property;

import com.google.j2objc.annotations.Weak;

/**
 * A controller for coordinating exam mode.
 * <p/>
 * <h3>Responsibilities</h3>
 * Here's the list of responsibilites of this controller:
 * <ul>
 *     <li><b>Exam state</b>: Starting, stopping, and finishing up the exam, and making the
 *     current exam state available (both as a getter, and as a change notification to registered
 *     listeners).
 *     <li><b>Start/end date</b>: Setting the exam start and end date, and providing date and
 *     time formatting for these.
 *     <li><b>Restrictions</b>: Applying restrictions on the {@link CommandDispatcher} at the
 *     start of an exam, and reverting those restrictions when the exam ends.
 *     <li><b>Events</b>: Collect relevant events (e.g., cheating attempts).
 * </ul>
 * <h3>NOT Responsibilities</h3>
 * Conversely, here's what's not in the responsibility of this controller:
 * <ul>
 *     <li><b>Clipboard</b>: The delegate is asked to clear the clipboard at the appropriate
 *     points. Why? Because I didn't want to introduce a clipboard abstraction (type) just for this
 *     single action.
 *     <li><b>Apps</b>: The delegate is asked to clear out the (other) apps at the appropriate
 *     points. Why? Because the App instances are managed (and owned) differently on the different
 *     platforms.
 * </ul>
 *
 *  @implNote This class is not designed to be thread-safe.
 */
public final class ExamController implements PropertiesRegistryListener {

	private static class ContextDependencies {
		@NonOwning
		final Object context;
		@NonOwning
		final CommandDispatcher commandDispatcher;
		@NonOwning
		final AlgebraProcessor algebraProcessor;
		@NonOwning
		final Set<ExamRestrictable> restrictables;

		ContextDependencies(Object context,
				CommandDispatcher commandDispatcher,
				AlgebraProcessor algebraProcessor,
				Set<ExamRestrictable> restrictables) {
			this.context = context;
			this.commandDispatcher = commandDispatcher;
			this.algebraProcessor = algebraProcessor;
			this.restrictables = restrictables;
		}
	}

	@Weak
	@NonOwning
	public ExamControllerDelegate delegate;

	@NonOwning
	private PropertiesRegistry propertiesRegistry;

	private Set<ContextDependencies> dependencies = new HashSet<>();
	private Set<ExamRestrictable> restrictables = new HashSet<>();
	private ContextDependencies activeDependencies;

	private ExamRegion examType;
	private ExamRestrictions examRestrictions;

	private ExamState state = ExamState.IDLE;
	private Date startDate, finishDate;
	private final Set<ExamListener> listeners = new HashSet<ExamListener>();
	private final TempStorage tempStorage = new TempStorage();
	private CheatingEvents cheatingEvents = new CheatingEvents();

	// TODO filter for apps with no CAS
//	private final CommandFilter noCASFilter = CommandFilterFactory.createNoCasCommandFilter();

	/**
	 * Creates a new ExamController. The ExamController will register itself as a listener
	 * on the properties registry.
	 * @param propertiesRegistry The properties registry.
	 */
	public ExamController(PropertiesRegistry propertiesRegistry) {
		this.propertiesRegistry = propertiesRegistry;
		propertiesRegistry.addListener(this); // TODO make sure weak references are used
	}

	/**
	 * Sets the delegate.
	 * @param delegate The delegate.
	 * @apiNote It is assumed that the delegate is set before attempting to start an exam.
	 * @implNote This method is provided for J2ObjC.
	 */
	public void setDelegate(@NonOwning ExamControllerDelegate delegate) {
		this.delegate = delegate;
	}

	/**
	 * Set the active context and associated dependencies.
	 *
	 * The context can be <i>any object</i>, but it should correspond to or identify the current
	 * app, or, in Suite, the currently active sub-app (Graphing, Geometry, etc). The only
	 * requirement here is that when any of the dependencies (currently, the command dispatcher and
	 * algebra processor) change, this should also mean a change in current context and be
	 * communicated to the exam controller by calling this method.
	 *
	 * This method needs to be called before an exam starts, and also when the active context
	 * changes during an exam, so what we can revert the restrictions on the current dependencies,
	 * and apply the restrictions on the new dependencies.
	 */
	public void setActiveContext(Object context,
			CommandDispatcher commandDispatcher,
			AlgebraProcessor algebraProcessor) {
		// revert restrictions for current dependencies, if exam is active
		if (examRestrictions != null && activeDependencies != null) {
			revertRestrictions(activeDependencies);
			restrictables = new HashSet<>();
		}
		activeDependencies = new ContextDependencies(context,
				commandDispatcher,
				algebraProcessor,
				restrictables);
		// apply restrictions to new dependencies, if exam is active
		if (examRestrictions != null) {
			applyRestrictions(examType, activeDependencies);
		}
	}

	/**
	 * Register an object that may need to apply additional restrictions/customization
	 * for certain types of exams.
	 *
	 * @param restrictable An object that may need to perform additional customization
	 * when an exam is started.
	 */
	public void registerRestrictable(ExamRestrictable restrictable) {
		restrictables.add(restrictable); // this may be shared with activeDependencies, if non-null
		if (examRestrictions != null) {
			restrictable.applyRestrictions(examRestrictions);
		}
	}

	/**
	 * Adds an {@link ExamListener}.
	 * @param listener The listener to add.
	 * Trying to add a listener that is already registered will have no effect.
	 */
	public void addListener(@NonOwning ExamListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes an `ExamListener`.
	 * @param listener The listener to remove.
	 * Trying to remove a listener that is not registered will have no effect.
	 */
	public void removeListener(ExamListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @return The current exam state.
	 * <p/>
	 * Also observable through {@link ExamListener#examStateChanged(ExamState) examStateChanged()}.
	 */
	public ExamState getState() {
		return state;
	}

	/**
	 * Changes state and notifies listeners about the state change.
	 *
	 * Note: If newState is equal to the current state, nothing happens.
	 * @param newState The new state to change to.
	 */
	private void setState(ExamState newState) {
		if (newState == state) {
			return;
		}
		state = newState;
		notifyListeners(newState);
	}

	/**
	 * @return The exam start date, if an exam is currently active, or null otherwise.
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @return The exam end date, if the exam has been stopped, or null otherwise.
	 */
	public Date getFinishDate() {
		return finishDate;
	}

	/**
	 * @return The current exam duration in seconds. If the exam is currently active
	 */
	public Double getDuration() {
		if (startDate == null) {
			return 0.0;
		}
		Date untilDate = state == ExamState.ACTIVE ? new Date() : finishDate;
		return (untilDate.getTime() - startDate.getTime()) * 1000.0;
	}

	/**
	 * Get ready for a new exam.
	 * @throws IllegalStateException if the exam controller is not in the {@link ExamState#IDLE IDLE}
	 * state.
	 */
	public void prepareExam() {
		if (state != ExamState.IDLE) {
			throw new IllegalStateException("expected to be in IDLE state, but is " + state);
		}
		setState(ExamState.PREPARING);
		// save current material and restore after exit?
	}

	/**
	 * Starts the exam.
	 * @throws IllegalStateException if the exam controller is not in the
	 * {@link ExamState#PREPARING PREPARING} state.
	 */
	public void startExam(ExamRegion examType) {
		if (state != ExamState.PREPARING) {
			throw new IllegalStateException("expected to be in PREPARING state, but is " + state);
		}
		if (activeDependencies == null) {
			throw new IllegalStateException("no active context; call setActiveContext() before attempting to start the exam");
		}
		this.examType = examType;
		applyRestrictions(examType, activeDependencies);

		tempStorage.clearTempMaterials();
		Material material = tempStorage.newMaterial();

		if (delegate != null) {
			delegate.requestClearApps();
			delegate.requestClearClipboard();
			delegate.requestSetActiveMaterial(material);
		}

		cheatingEvents = new CheatingEvents();
		startDate = new Date();
		setState(ExamState.ACTIVE); // TODO suppress syntax in CommandErrorMessageBuilder
	}

	/**
	 * Finishes the current exam.
	 * @throws IllegalStateException if the exam controller is not in the {@link ExamState#ACTIVE ACTIVE}
	 * state.
	 */
	public void finishExam() {
		if (state != ExamState.ACTIVE) {
			throw new IllegalStateException("expected to be in ACTIVE state, but is " + state);
		}
		finishDate = new Date();
		setState(ExamState.FINISHED);
	}

	/**
	 * Exits the exam.
	 * @throws IllegalStateException if the exam controller is not in the
	 * {@link ExamState#FINISHED FINISHED} state.
	 */
	public void exitExam() {
		if (state != ExamState.FINISHED) {
			throw new IllegalStateException("expected to be in FINISHED state, but is " + state);
		}
		revertRestrictions(activeDependencies);
		tempStorage.clearTempMaterials();
		if (delegate != null) {
			delegate.requestClearApps();
			delegate.requestClearClipboard();
//			delegate.requestSetActiveMaterial(); ?? create new empty material?
		}
		startDate = finishDate = null;
		setState(ExamState.IDLE);
	}

	public boolean isExamActive() {
		return state == ExamState.ACTIVE;
	}

	public boolean isIdle() {
		return state == ExamState.IDLE;
	}

	private void notifyListeners(ExamState newState) {
		for (ExamListener listener : listeners) {
			listener.examStateChanged(newState);
		}
	}

	private void applyRestrictions(ExamRegion examType, ContextDependencies dependencies) {
		// TODO app.resetCommandDict() (register as ExamRestrictable?)
		ExamRestrictions newRestrictions = ExamRestrictions.forExamType(examType);
		if (newRestrictions == null) {
			return; // log/throw?
		}
		if (delegate != null) {
			if (newRestrictions.getDisabledSubApps().contains(delegate.getCurrentSubApp())) {
				delegate.requestSwitchSubApp(newRestrictions.getDefaultSubApp());
			}
		}
		examRestrictions = newRestrictions;
		if (dependencies != null) {
			examRestrictions.apply(dependencies.commandDispatcher,
					dependencies.algebraProcessor,
					propertiesRegistry,
					dependencies.context);
			for (ExamRestrictable restrictable : dependencies.restrictables) {
				restrictable.applyRestrictions(examRestrictions);
			}
			// TODO suppress syntax in CommandErrorMessageBuilder (register as ExamRestrictable?)
		}
	}

	private void revertRestrictions(ContextDependencies dependencies) {
		if (examRestrictions == null) {
			return;
		}
		if (dependencies != null) {
			examRestrictions.revert(dependencies.commandDispatcher,
					dependencies.algebraProcessor,
					propertiesRegistry,
					dependencies.context);
			for (ExamRestrictable restrictable : dependencies.restrictables) {
				restrictable.revertRestrictions(examRestrictions);
			}
			// TODO enable syntax in CommandErrorMessageBuilder (register as ExamRestrictable?)
		}
	}

	public TempStorage getTempStorage() {
		return tempStorage;
	}

	// PropertiesRegistryListener

	@Override
	public void propertyRegistered(Property property, Object context) {
		if (examRestrictions != null) {
			examRestrictions.propertyRegistered(property);
		}
	}

	@Override
	public void propertyUnregistered(Property property, Object context) {
	}
}
