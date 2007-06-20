<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@ taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@ taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<tiles:insert definition=".view">
	<tiles:put name="body" type="string">
		<table width="95%" border="0" cellpadding="0" cellspacing="0">
	      <tr>
	        <td class="bluetablehead05"><span class="fontnormal8pt">
				<html-el:link href="loanproductaction.do?method=cancelCreate&randomNUm=${sessionScope.randomNUm}&currentFlowKey=${requestScope.currentFlowKey}">
					<mifos:mifoslabel name="product.admin" bundle="ProductDefUIResources" />
				</html-el:link> / </span> <span class="fontnormal8ptbold"> <mifos:mifoslabel name="admin.viewreports"  /></span>
			</td>
	      </tr>
	    </table>

      <table width="95%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td align="left" valign="top" class="paddingL15T15"><table width="95%" border="0" cellpadding="3" cellspacing="0">
            <tr>
              <td class="headingorange"><span class="headingorange"><mifos:mifoslabel name="admin.viewreports" /></span></td>
            </tr>
            <tr>
              <td valign="top" class="fontnormalbold"> <span class="fontnormal">
				 <mifos:mifoslabel name="admin.infoclickonlabel" /> <mifos:mifoslabel name="reports.labelinfo" bundle="reportsUIResources" /> 
						&nbsp;<html-el:link
						href="birtReportsUploadAction.do?method=getBirtReportsUploadPage&viewPath=administerreports_path">
						<mifos:mifoslabel name="reports.linkinfo" bundle="reportsUIResources" />
					</html-el:link>              <br>
                  <br>
              </span><span class="fontnormalbold"> </span><span class="fontnormalbold"> </span>
              <table width="75%" border="0" cellpadding="3" cellspacing="0">
                  <tr>
                    <td height="30" colspan="2" class="blueline"><strong>
  					<mifos:mifoslabel name="reports.category" bundle="reportsUIResources"/> , 
               		 <mifos:mifoslabel name="reports.title" bundle="reportsUIResources"/>
					</strong></td>
                  </tr>
                  <c:forEach var="reportCategory" items="${sessionScope.listOfReports}" varStatus="loop" begin='0'>
					<c:forEach var="report" items="${reportCategory.reportsSet}">
	                  <tr>
	                    <td width="59%" height="30" class="blueline"><span class="fontnormal"><c:out value="${report.reportsCategoryBO.reportCategoryName}" />, 
	                    	<c:out value="${report.reportName}"/></span></td>
	                    <td width="41%" class="blueline"><a href=""><mifos:mifoslabel name = "reports.linkviewtemplate" bundle="reportsUIResources" /></a> |  
									     <a href="birtReportsUploadAction.do?method=edit&reportId=<c:out value="${report.reportId}" />">
								            <mifos:mifoslabel name = "reports.edit" bundle="reportsUIResources" /></a></td>
	                  </tr>
					</c:forEach>
                  </c:forEach>
                </table>                <span class="fontnormalbold">                </span></td>
            </tr>
          </table>            <br></td>
        </tr>
      </table>

	</tiles:put>
</tiles:insert>
