// $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, useForLoop, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
/*******************************************************************************
 * Copyright (c) 2010 Ericsson Research Canada
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Description:
 * 
 * This class implements the dialog used to fill-in the Anomaly element details
 * This is a modeless-like dialog
 * 
 * Contributors:
 *   Sebastien Dubois - Created for Mylyn Review R4E project
 *   
 ******************************************************************************/

package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleClass;
import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleRank;
import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewVersionsException;
import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRootElement;
import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRule;
import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRuleArea;
import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRuleSet;
import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRuleViolation;
import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorContentProvider;
import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorLabelProvider;
import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorTreeViewer;
import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author lmcdubo
 * @version $Revision: 1.0 $
 */
public class AnomalyInputDialog extends FormDialog implements IAnomalyInputDialog {

	// ------------------------------------------------------------------------
	// Constants
	// ------------------------------------------------------------------------

	/**
	 * Field ADD_ANOMALY_DIALOG_TITLE. (value is ""Enter Anomaly details"")
	 */
	private static final String ADD_ANOMALY_DIALOG_TITLE = "Enter Anomaly Details";

	/**
	 * Field ADD_ANOMALY_DIALOG_VALUE. (value is ""Enter the Anomaly title:"")
	 */
	private static final String ADD_ANOMALY_DIALOG_VALUE = "Anomaly Title: ";

	/**
	 * Field ADD_COMMENT_DIALOG_VALUE. (value is ""Enter your comments for the new Anomaly:"")
	 */
	private static final String ADD_DESCRIPTION_DIALOG_VALUE = "Anomaly Description: ";

	/**
	 * Field BASIC_PARAMS_HEADER_MSG. (value is ""Enter the mandatory basic parameters for this anomaly"")
	 */
	private static final String BASIC_PARAMS_HEADER_MSG = "Enter the mandatory basic parameters for this anomaly";

	/**
	 * Field EXTRA_PARAMS_HEADER_MSG. (value is ""Enter the optional extra parameters for this Review Group"")
	 */
	private static final String EXTRA_PARAMS_HEADER_MSG = "Enter the optional extra parameters for this Review";

	/**
	 * Field ADD_RULE_DIALOG_VALUE. (value is ""Rule: "")
	 */
	private static final String ADD_RULE_DIALOG_VALUE = "Rule Tree "
			+ "(Take note that the Anomaly will be created with values taken from the selected Design Rule)";

	/**
	 * Field DEFAULT_ELEMENT_COLUMN_WIDTH. (value is "150")
	 */
	private static final int DEFAULT_ELEMENT_COLUMN_WIDTH = 150;

	/**
	 * Field DEFAULT_TREE_COLUMN_WIDTH. (value is "100")
	 */
	private static final int DEFAULT_TREE_COLUMN_WIDTH = 100;

	// ------------------------------------------------------------------------
	// Member variables
	// ------------------------------------------------------------------------

	/**
	 * Field fAnomalyTitleValue.
	 */
	private String fAnomalyTitleValue = "";

	/**
	 * Field fAnomalyTitleInputTextField.
	 */
	protected Text fAnomalyTitleInputTextField = null;

	/**
	 * Field fAnomalyDescriptionValue.
	 */
	private String fAnomalyDescriptionValue = "";

	/**
	 * Field fAnomalyDescriptionInputTextField.
	 */
	protected Text fAnomalyDescriptionInputTextField;

	/**
	 * Field fAnomalyClassValue.
	 */
	private R4EDesignRuleClass fAnomalyClassValue = null;

	/**
	 * Field fAnomalyClass.
	 */
	protected CCombo fAnomalyClass = null;

	/**
	 * Field fAnomalyRankValue.
	 */
	private R4EDesignRuleRank fAnomalyRankValue = null;

	/**
	 * Field fAnomalyRank.
	 */
	protected CCombo fAnomalyRank = null;

	/**
	 * Field fDateText.
	 */
	protected Text fDateText = null;

	/**
	 * Field fAnomalyDueDateValue.
	 */
	private Date fAnomalyDueDateValue = null;

	/**
	 * Field fRuleTreeViewer.
	 */
	protected TreeViewer fRuleTreeViewer = null;

	/**
	 * Field fRuleReferenceValue.
	 */
	private R4EUIRule fRuleReferenceValue = null;

	/**
	 * The input validator, or <code>null</code> if none.
	 */
	private final IInputValidator fValidator;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * Constructor for R4EAnomalyInputDialog.
	 * 
	 * @param aParentShell
	 *            Shell
	 */
	public AnomalyInputDialog(Shell aParentShell) {
		super(aParentShell);
		setBlockOnOpen(false);
		fValidator = new R4EInputValidator();
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * Method buttonPressed.
	 * 
	 * @param buttonId
	 *            int
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

			//Validate Anomaly Title
			String validateResult = validateEmptyInput(fAnomalyTitleInputTextField);
			if (null != validateResult) {
				//Validation of input failed
				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
						"No input given for Anomaly Title", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
								validateResult, null), IStatus.ERROR);
				dialog.open();
				this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				return;
			}
			fAnomalyTitleValue = fAnomalyTitleInputTextField.getText();

			//Validate Anomaly Comment
			validateResult = validateEmptyInput(fAnomalyDescriptionInputTextField);
			if (null != validateResult) {
				//Validation of input failed
				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
						"No input given for Anomaly Comment", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
								validateResult, null), IStatus.ERROR);
				dialog.open();
				this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				return;
			}
			fAnomalyDescriptionValue = fAnomalyDescriptionInputTextField.getText();

			//Validate R4EUIRule (if present)
			fRuleReferenceValue = null;
			if (fRuleTreeViewer.getSelection() instanceof IStructuredSelection) {
				final IStructuredSelection selection = (IStructuredSelection) fRuleTreeViewer.getSelection();
				if (null != selection) {
					fRuleReferenceValue = (R4EUIRule) selection.getFirstElement();
				}
			}

			fAnomalyRankValue = UIUtils.getRankFromString(fAnomalyRank.getText());
			fAnomalyClassValue = UIUtils.getClassFromString(fAnomalyClass.getText());
		} else {
			fAnomalyTitleValue = null;
			fAnomalyDescriptionValue = null;
			fRuleReferenceValue = null;
			fAnomalyRankValue = null;
			fAnomalyClassValue = null;
			fAnomalyDueDateValue = null;
		}
		R4EUIModelController.setJobInProgress(false); //Do this here to refresh the view properly
		this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		super.buttonPressed(buttonId);
	}

	/**
	 * Method configureShell.
	 * 
	 * @param shell
	 *            Shell
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(ADD_ANOMALY_DIALOG_TITLE);
		shell.setMinimumSize(R4EUIConstants.DIALOG_DEFAULT_WIDTH, R4EUIConstants.DIALOG_DEFAULT_HEIGHT);
	}

	/**
	 * Configures the dialog form and creates form content. Clients should override this method.
	 * 
	 * @param mform
	 *            the dialog form
	 */
	@Override
	protected void createFormContent(final IManagedForm mform) {

		final FormToolkit toolkit = mform.getToolkit();
		final ScrolledForm sform = mform.getForm();
		sform.setExpandVertical(true);
		final Composite composite = sform.getBody();
		final GridLayout layout = new GridLayout(4, false);
		composite.setLayout(layout);
		GridData textGridData = null;

		//Basic parameters section
		final Section basicSection = toolkit.createSection(composite, Section.DESCRIPTION
				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		final GridData basicSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		basicSectionGridData.horizontalSpan = 4;
		basicSection.setLayoutData(basicSectionGridData);
		basicSection.setText(R4EUIConstants.BASIC_PARAMS_HEADER);
		basicSection.setDescription(BASIC_PARAMS_HEADER_MSG);
		basicSection.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		final Composite basicSectionClient = toolkit.createComposite(basicSection);
		basicSectionClient.setLayout(layout);
		basicSection.setClient(basicSectionClient);

		//Anomaly Title
		Label label = toolkit.createLabel(basicSectionClient, ADD_ANOMALY_DIALOG_VALUE);
		label.setToolTipText(R4EUIConstants.ANOMALY_TITLE_TOOLTIP);
		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		fAnomalyTitleInputTextField = toolkit.createText(basicSectionClient, "", SWT.SINGLE | SWT.BORDER);
		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		textGridData.horizontalSpan = 3;
		fAnomalyTitleInputTextField.setToolTipText(R4EUIConstants.ANOMALY_TITLE_TOOLTIP);
		fAnomalyTitleInputTextField.setLayoutData(textGridData);
		fAnomalyTitleInputTextField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// ignore
				if (fAnomalyTitleInputTextField.getText().length() > 0
						&& fAnomalyDescriptionInputTextField.getText().length() > 0) {
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				} else {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
			}
		});

		//Anomaly Description
		label = toolkit.createLabel(basicSectionClient, ADD_DESCRIPTION_DIALOG_VALUE);
		label.setToolTipText(R4EUIConstants.ANOMALY_DESCRIPTION_TOOLTIP);
		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		fAnomalyDescriptionInputTextField = toolkit.createText(basicSectionClient, "", SWT.MULTI | SWT.V_SCROLL
				| SWT.BORDER);
		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		textGridData.horizontalSpan = 3;
		textGridData.heightHint = fAnomalyTitleInputTextField.getLineHeight() * 7;
		fAnomalyDescriptionInputTextField.setToolTipText(R4EUIConstants.ANOMALY_DESCRIPTION_TOOLTIP);
		fAnomalyDescriptionInputTextField.setLayoutData(textGridData);
		fAnomalyDescriptionInputTextField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// ignore
				if (fAnomalyTitleInputTextField.getText().length() > 0
						&& fAnomalyDescriptionInputTextField.getText().length() > 0) {
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				} else {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
			}
		});

		//Extra parameters section
		final Section extraSection = toolkit.createSection(composite, Section.DESCRIPTION
				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		final GridData extraSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		extraSectionGridData.horizontalSpan = 4;
		extraSection.setLayoutData(extraSectionGridData);
		extraSection.setText(R4EUIConstants.EXTRA_PARAMS_HEADER);
		extraSection.setDescription(EXTRA_PARAMS_HEADER_MSG);
		extraSection.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		final Composite extraSectionClient = toolkit.createComposite(extraSection);
		extraSectionClient.setLayout(layout);
		extraSection.setClient(extraSectionClient);

		//Anomaly Class
		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.CLASS_LABEL);
		label.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

		fAnomalyClass = new CCombo(extraSectionClient, SWT.BORDER | SWT.READ_ONLY);
		fAnomalyClass.setItems(UIUtils.getClasses());
		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		textGridData.horizontalSpan = 3;
		fAnomalyClass.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
		fAnomalyClass.setLayoutData(textGridData);

		//Anomaly Rank 	
		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.RANK_LABEL);
		label.setToolTipText(R4EUIConstants.ANOMALY_RANK_TOOLTIP);
		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

		fAnomalyRank = new CCombo(extraSectionClient, SWT.BORDER | SWT.READ_ONLY);
		fAnomalyRank.setItems(UIUtils.getRanks());
		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		textGridData.horizontalSpan = 3;
		fAnomalyRank.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
		fAnomalyRank.setLayoutData(textGridData);

		//Due Date
		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.DUE_DATE_LABEL);
		textGridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		textGridData.horizontalSpan = 1;
		label.setLayoutData(textGridData);

		final Composite dateComposite = toolkit.createComposite(extraSectionClient);
		textGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		textGridData.horizontalSpan = 3;
		dateComposite.setToolTipText(R4EUIConstants.ANOMALY_DUE_DATE_TOOLTIP);
		dateComposite.setLayoutData(textGridData);
		dateComposite.setLayout(new GridLayout(2, false));

		fDateText = toolkit.createText(dateComposite, "", SWT.BORDER | SWT.READ_ONLY);
		fDateText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		fDateText.setEditable(false);

		Button calendarButton = toolkit.createButton(dateComposite, "...", SWT.NONE);
		calendarButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		calendarButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				final ICalendarDialog dialog = R4EUIDialogFactory.getInstance().getCalendarDialog();
				final int result = dialog.open();
				if (result == Window.OK) {
					final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT);
					fDateText.setText(dateFormat.format(dialog.getDate()));
					fAnomalyDueDateValue = dialog.getDate();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
				// No implementation needed
			}
		});

		//Rule Tree
		label = toolkit.createLabel(extraSectionClient, ADD_RULE_DIALOG_VALUE);
		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		textGridData.horizontalSpan = 4;
		label.setLayoutData(textGridData);

		fRuleTreeViewer = new ReviewNavigatorTreeViewer(extraSectionClient, SWT.FULL_SELECTION | SWT.BORDER
				| SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		fRuleTreeViewer.setContentProvider(new ReviewNavigatorContentProvider());
		fRuleTreeViewer.getTree().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(fRuleTreeViewer, ToolTip.NO_RECREATE);

		final TreeViewerColumn elementColumn = new TreeViewerColumn(fRuleTreeViewer, SWT.NONE);
		elementColumn.getColumn().setText("Rule Tree");
		elementColumn.getColumn().setWidth(DEFAULT_ELEMENT_COLUMN_WIDTH);
		elementColumn.setLabelProvider(new ReviewNavigatorLabelProvider() {
			@Override
			public String getToolTipText(Object element) {
				if (element instanceof R4EUIRule) {
					return ((R4EUIRule) element).getRule().getDescription();
				}
				return null;
			}
		});

		final TreeViewerColumn titleColumn = new TreeViewerColumn(fRuleTreeViewer, SWT.NONE);
		titleColumn.getColumn().setText(R4EUIConstants.TITLE_LABEL);
		titleColumn.getColumn().setWidth(DEFAULT_TREE_COLUMN_WIDTH);
		titleColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof R4EUIRule) {
					return ((R4EUIRule) element).getRule().getTitle();
				}
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof R4EUIRule) {
					return ((R4EUIRule) element).getRule().getDescription();
				}
				return null;
			}

			@Override
			public Point getToolTipShift(Object object) {
				return new Point(R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_X, R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_Y);
			}

			@Override
			public int getToolTipDisplayDelayTime(Object object) {
				return R4EUIConstants.TOOLTIP_DISPLAY_DELAY;
			}

			@Override
			public int getToolTipTimeDisplayed(Object object) {
				return R4EUIConstants.TOOLTIP_DISPLAY_TIME;
			}

			@Override
			public void update(ViewerCell cell) {
				final Object element = cell.getElement();
				if (element instanceof R4EUIRule) {
					cell.setText(((R4EUIRule) element).getRule().getTitle());
				} else {
					cell.setText(null);
				}
			}
		});

		final TreeViewerColumn classColumn = new TreeViewerColumn(fRuleTreeViewer, SWT.NONE);
		classColumn.getColumn().setText(R4EUIConstants.CLASS_LABEL);
		classColumn.getColumn().setWidth(DEFAULT_TREE_COLUMN_WIDTH);
		classColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof R4EUIRule) {
					return getClassStr(((R4EUIRule) element).getRule().getClass_());
				}
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof R4EUIRule) {
					return ((R4EUIRule) element).getRule().getDescription();
				}
				return null;
			}

			@Override
			public Point getToolTipShift(Object object) {
				return new Point(R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_X, R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_Y);
			}

			@Override
			public int getToolTipDisplayDelayTime(Object object) {
				return R4EUIConstants.TOOLTIP_DISPLAY_DELAY;
			}

			@Override
			public int getToolTipTimeDisplayed(Object object) {
				return R4EUIConstants.TOOLTIP_DISPLAY_TIME;
			}

			@Override
			public void update(ViewerCell cell) {
				final Object element = cell.getElement();
				if (element instanceof R4EUIRule) {
					cell.setText(getClassStr(((R4EUIRule) element).getRule().getClass_()));
				} else {
					cell.setText(null);
				}
			}
		});

		final TreeViewerColumn rankColumn = new TreeViewerColumn(fRuleTreeViewer, SWT.NONE);
		rankColumn.getColumn().setText(R4EUIConstants.RANK_LABEL);
		rankColumn.getColumn().setWidth(DEFAULT_TREE_COLUMN_WIDTH);
		rankColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof R4EUIRule) {
					return getRankStr(((R4EUIRule) element).getRule().getRank());
				}
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof R4EUIRule) {
					return ((R4EUIRule) element).getRule().getDescription();
				}
				return null;
			}

			@Override
			public Point getToolTipShift(Object object) {
				return new Point(R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_X, R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_Y);
			}

			@Override
			public int getToolTipDisplayDelayTime(Object object) {
				return R4EUIConstants.TOOLTIP_DISPLAY_DELAY;
			}

			@Override
			public int getToolTipTimeDisplayed(Object object) {
				return R4EUIConstants.TOOLTIP_DISPLAY_TIME;
			}

			@Override
			public void update(ViewerCell cell) {
				final Object element = cell.getElement();
				if (element instanceof R4EUIRule) {
					cell.setText(getRankStr(((R4EUIRule) element).getRule().getRank()));
				} else {
					cell.setText(null);
				}
			}
		});

		fRuleTreeViewer.setInput(R4EUIModelController.getRootElement());

		fRuleTreeViewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				//Only display rule sets that are included in the parent review group
				if (element instanceof R4EUIRuleSet || element instanceof R4EUIRuleArea
						|| element instanceof R4EUIRuleViolation || element instanceof R4EUIRule) {
					//Get parent RuleSet
					IR4EUIModelElement parentRuleSetElement = (IR4EUIModelElement) element;
					while (!(parentRuleSetElement instanceof R4EUIRuleSet) && null != parentRuleSetElement.getParent()) {
						if (!parentRuleSetElement.isEnabled()) {
							return false;
						}
						parentRuleSetElement = parentRuleSetElement.getParent();
					}
					//If the current reveiw group contains a reference to this Rule Set, display it
					if ((((R4EUIReviewGroup) R4EUIModelController.getActiveReview().getParent()).getRuleSets().contains(parentRuleSetElement))) {
						if (!parentRuleSetElement.isOpen()) {
							try {
								parentRuleSetElement.open();
							} catch (FileNotFoundException e) {
								R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage()
										+ ")");
								R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
							} catch (ResourceHandlingException e) {
								R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage()
										+ ")");
								R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
							} catch (ReviewVersionsException e) {
								R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage()
										+ ")");
								R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
							}
						}
						return true;
					}
				}
				return false;
			}
		});
		fRuleTreeViewer.expandAll();
		fRuleTreeViewer.refresh();

		textGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		textGridData.horizontalSpan = 4;
		fRuleTreeViewer.getTree().setLayoutData(textGridData);

		fRuleTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				//Only Rules are selectable
				if (event.getSelection() instanceof IStructuredSelection) {
					if (null == ((IStructuredSelection) event.getSelection()).getFirstElement()) {
						return;
					}
					if (((IStructuredSelection) event.getSelection()).getFirstElement() instanceof R4EUIRule) {
						final R4EUIRule rule = (R4EUIRule) ((IStructuredSelection) event.getSelection()).getFirstElement();
						fAnomalyTitleInputTextField.setText(rule.getRule().getTitle());
						fAnomalyDescriptionInputTextField.setText(rule.getRule().getDescription());
						fAnomalyClass.select(rule.getRule().getClass_().getValue());
						fAnomalyRank.select(rule.getRule().getRank().getValue());
						fAnomalyClass.setEnabled(false);
						fAnomalyRank.setEnabled(false);
						return;
					}
				}
				fRuleTreeViewer.setSelection(null);
				fAnomalyClass.setEnabled(true);
				fAnomalyRank.setEnabled(true);
			}
		});
	}

	/**
	 * Configures the button bar.
	 * 
	 * @param parent
	 *            the parent composite
	 * @return Control
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		final Control bar = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return bar;
	}

	/**
	 * Method isResizable.
	 * 
	 * @return boolean
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * Returns the string typed into this input dialog.
	 * 
	 * @return the anomaly title input string
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getAnomalyTitleValue()
	 */
	public String getAnomalyTitleValue() {
		return fAnomalyTitleValue;
	}

	/**
	 * Returns the string typed into this input dialog.
	 * 
	 * @return the anomaly description input string
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getAnomalyDescriptionValue()
	 */
	public String getAnomalyDescriptionValue() {
		return fAnomalyDescriptionValue;
	}

	/**
	 * Returns the string typed into this input dialog.
	 * 
	 * @return the R4EUIRule reference (if any)
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getRuleReferenceValue()
	 */
	public R4EUIRule getRuleReferenceValue() {
		return fRuleReferenceValue;
	}

	/**
	 * Method validateEmptyInput.
	 * 
	 * @param aText
	 *            Text
	 * @return String
	 */
	private String validateEmptyInput(Text aText) {
		return fValidator.isValid(aText.getText());
	}

	/**
	 * Method setShellStyle.
	 * 
	 * @param newShellStyle
	 *            int
	 */
	@Override
	protected void setShellStyle(int newShellStyle) {
		int newstyle = newShellStyle & ~SWT.APPLICATION_MODAL; /* turn off APPLICATION_MODAL */
		newstyle |= SWT.MODELESS; /* turn on MODELESS */
		super.setShellStyle(newstyle);
	}

	/**
	 * Method open.
	 * 
	 * @return int
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#open()
	 */
	@Override
	public int open() {
		super.open();
		pumpMessages(); /* this will let the caller wait till OK, Cancel is pressed, but will let the other GUI responsive */
		return super.getReturnCode();
	}

	/**
	 * Method pumpMessages.
	 */
	protected void pumpMessages() {
		final Shell sh = getShell();
		final Display disp = sh.getDisplay();
		while (!sh.isDisposed()) { // $codepro.audit.disable methodInvocationInLoopCondition
			if (!disp.readAndDispatch()) {
				disp.sleep();
			}
		}
		disp.update();
	}

	/**
	 * Method getClassStr.
	 * 
	 * @param aClass
	 *            R4EDesignRuleClass
	 * @return String
	 */
	protected String getClassStr(R4EDesignRuleClass aClass) {
		if (aClass.equals(R4EDesignRuleClass.R4E_CLASS_ERRONEOUS)) {
			return R4EUIConstants.ANOMALY_CLASS_ERRONEOUS;
		} else if (aClass.equals(R4EDesignRuleClass.R4E_CLASS_SUPERFLUOUS)) {
			return R4EUIConstants.ANOMALY_CLASS_SUPERFLUOUS;
		} else if (aClass.equals(R4EDesignRuleClass.R4E_CLASS_IMPROVEMENT)) {
			return R4EUIConstants.ANOMALY_CLASS_IMPROVEMENT;
		} else if (aClass.equals(R4EDesignRuleClass.R4E_CLASS_QUESTION)) {
			return R4EUIConstants.ANOMALY_CLASS_QUESTION;
		} else {
			return null; //should never happen
		}
	}

	/**
	 * Method setClass_.
	 * 
	 * @param aClass
	 *            R4EDesignRuleClass
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setClass_(R4EDesignRuleClass)
	 */
	public void setClass_(R4EDesignRuleClass aClass) {
		fAnomalyClassValue = aClass;
		if (null != fAnomalyClassValue) {
			fAnomalyClass.setText(getClassStr(fAnomalyClassValue));
		}
	}

	/**
	 * Method getClass_.
	 * 
	 * @return R4EDesignRuleClass
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getClass_()
	 */
	public R4EDesignRuleClass getClass_() {
		return fAnomalyClassValue;
	}

	/**
	 * Method getRankStr.
	 * 
	 * @param aRank
	 *            R4EDesignRuleRank
	 * @return String
	 */
	protected String getRankStr(R4EDesignRuleRank aRank) {
		if (aRank.equals(R4EDesignRuleRank.R4E_RANK_NONE)) {
			return R4EUIConstants.ANOMALY_RANK_NONE;
		} else if (aRank.equals(R4EDesignRuleRank.R4E_RANK_MINOR)) {
			return R4EUIConstants.ANOMALY_RANK_MINOR;
		} else if (aRank.equals(R4EDesignRuleRank.R4E_RANK_MAJOR)) {
			return R4EUIConstants.ANOMALY_RANK_MAJOR;
		} else {
			return null; //should never happen
		}
	}

	/**
	 * Method setRank.
	 * 
	 * @param aRank
	 *            R4EDesignRuleRank
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setRank(R4EDesignRuleRank)
	 */
	public void setRank(R4EDesignRuleRank aRank) {
		fAnomalyRankValue = aRank;
		if (null != fAnomalyRankValue) {
			fAnomalyRank.setText(getRankStr(fAnomalyRankValue));
		}
	}

	/**
	 * Method getRank.
	 * 
	 * @return R4EDesignRuleRank
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getRank()
	 */
	public R4EDesignRuleRank getRank() {
		return fAnomalyRankValue;
	}

	/**
	 * Method setDueDate.
	 * 
	 * @param aDate
	 *            Date
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setDueDate(Date)
	 */
	public void setDueDate(Date aDate) {
		fAnomalyDueDateValue = aDate;
		final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT);
		if (null != fAnomalyDueDateValue) {
			fDateText.setText(dateFormat.format(fAnomalyDueDateValue));
		}
	}

	/**
	 * Method getDueDate.
	 * 
	 * @return Date
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getDueDate()
	 */
	public Date getDueDate() {
		return fAnomalyDueDateValue;
	}

	/**
	 * Method setTitle.
	 * 
	 * @param aTitle
	 *            String
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setTitle(String)
	 */
	public void setTitle(String aTitle) {
		fAnomalyTitleInputTextField.setText(aTitle);
	}

	/**
	 * Method setDescription.
	 * 
	 * @param aDescription
	 *            String
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setDescription(String)
	 */
	public void setDescription(String aDescription) {
		fAnomalyDescriptionInputTextField.setText(aDescription);
	}

	/**
	 * Method setRuleID.
	 * 
	 * @param aId
	 *            String
	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setRuleID(String)
	 */
	public void setRuleID(String aId) {
		List<R4EUIRuleSet> ruleSets = ((R4EUIRootElement) R4EUIModelController.getRootElement()).getRuleSets();
		for (R4EUIRuleSet ruleSet : ruleSets) {
			for (IR4EUIModelElement area : ruleSet.getChildren()) {
				for (IR4EUIModelElement violation : area.getChildren()) {
					for (IR4EUIModelElement rule : violation.getChildren()) {
						if (((R4EUIRule) rule).getRule().getId().equals(aId)) {
							fRuleTreeViewer.setSelection(new StructuredSelection(rule), true);
							return;
						}
					}
				}
			}
		}
	}
}
