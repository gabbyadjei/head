package org.mifos.application.accounts.struts.actionforms;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.ValidatorActionForm;
import org.mifos.application.accounts.loan.util.helpers.LoanConstants;
import org.mifos.application.accounts.savings.util.helpers.SavingsConstants;
import org.mifos.application.accounts.util.helpers.AccountConstants;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.util.helpers.Methods;
import org.mifos.framework.util.helpers.StringUtils;

public class EditStatusActionForm extends ValidatorActionForm {
	private String accountId;

	private String globalAccountNum;

	private String accountName;
	
	private String accountTypeId;

	private String currentStatusId;

	private String newStatusId;

	private String flagId;

	private String notes;

	private String[] selectedItems;
	
	private String input;
	
	private String commentDate;

	public String getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(String commentDate) {
		this.commentDate = commentDate;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountTypeId() {
		return accountTypeId;
	}

	public void setAccountTypeId(String accountTypeId) {
		this.accountTypeId = accountTypeId;
	}

	public String getCurrentStatusId() {
		return currentStatusId;
	}

	public void setCurrentStatusId(String currentStatusId) {
		this.currentStatusId = currentStatusId;
	}

	public String getFlagId() {
		return flagId;
	}

	public void setFlagId(String flagId) {
		this.flagId = flagId;
	}

	public String getGlobalAccountNum() {
		return globalAccountNum;
	}

	public void setGlobalAccountNum(String globalAccountNum) {
		this.globalAccountNum = globalAccountNum;
	}

	public String getNewStatusId() {
		return newStatusId;
	}

	public void setNewStatusId(String newStatusId) {
		this.newStatusId = newStatusId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String[] getSelectedItems() {
		return selectedItems;
	}

	public void setSelectedItems(String[] selectedItems) {
		this.selectedItems = selectedItems;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		String methodCalled = request.getParameter(Methods.method.toString());
		if (null != methodCalled) {
			if ((Methods.preview.toString()).equals(methodCalled)) {
				handleStatusPreviewValidations(request, errors);
			} else if ((Methods.update.toString()).equals(methodCalled)) {
				handleUpdateStatus(request, errors);
			}
		}
		if (null != errors && !errors.isEmpty()) {
			request.setAttribute(Globals.ERROR_KEY, errors);
			request.setAttribute("methodCalled", methodCalled);
		}
		return errors;
	}

	private ActionErrors handleUpdateStatus(HttpServletRequest request,
			ActionErrors errors) {
		String chklistSize = request.getParameter("chklistSize");
		if(request.getParameter("selectedItems")==null){
			selectedItems=null;
		}
		if(StringUtils.isNullSafe(chklistSize)) {
			if (isCheckListNotComplete(chklistSize))
				addError(errors, LoanConstants.INCOMPLETE_CHECKLIST,LoanConstants.INCOMPLETE_CHECKLIST);
		}
		return errors;
	}

	private ActionErrors handleStatusPreviewValidations(
			HttpServletRequest request, ActionErrors errors) {
		if (!StringUtils.isNullSafe(newStatusId))
			addError(errors, LoanConstants.MANDATORY,LoanConstants.MANDATORY,
					AccountConstants.STATUS);
		else if (isNewStatusHasFlag() && !StringUtils.isNullAndEmptySafe(flagId))
			addError(errors, LoanConstants.MANDATORY_SELECT,LoanConstants.MANDATORY_SELECT,
					SavingsConstants.FLAG);
		if (StringUtils.isNullOrEmpty(notes))
			addError(errors, LoanConstants.MANDATORY_TEXTBOX,LoanConstants.MANDATORY_TEXTBOX, AccountConstants.NOTES);
		else if (notes.length() > LoanConstants.COMMENT_LENGTH)
			addError(errors, LoanConstants.MAX_LENGTH,LoanConstants.MAX_LENGTH,
					AccountConstants.NOTES, String
							.valueOf(LoanConstants.COMMENT_LENGTH));
		return errors;
	}

	private void addError(ActionErrors errors, String property, String key,
			String... arg) {
		errors.add(property, new ActionMessage(key, arg));
	}

	private boolean isCheckListNotComplete(String chklistSize) {
		return (isPartialSelected(chklistSize) || isNoneSelected(chklistSize));
	}
	
	private boolean isPartialSelected(String chklistSize) {
		return (selectedItems != null) && (Integer.valueOf(chklistSize).intValue() != selectedItems.length);
	}
	
	private boolean isNoneSelected(String chklistSize) {
		return (Integer.valueOf(chklistSize).intValue() > 0) && (selectedItems == null);
	}
	
	private boolean isNewStatusHasFlag() {
		return (Short.valueOf(newStatusId).equals(AccountState.LOANACC_CANCEL.getValue())) || (Short.valueOf(newStatusId).equals(AccountState.SAVINGS_ACC_CANCEL.getValue()));
	}
}
