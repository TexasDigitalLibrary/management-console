<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insertDefinition name="base" >
	<tiles:putAttribute name="title">
		<spring:message code="add.space" />	
	</tiles:putAttribute>
	<tiles:putAttribute name="mainTab" value="spaces"/>
	<tiles:putAttribute name="menu" value=""/>
	<tiles:putAttribute name="main-content">
		<tiles:insertDefinition name="base-main-content">
			<tiles:putAttribute name="header">
				<tiles:insertDefinition name="base-content-header">
					<tiles:putAttribute name="title">
						<spring:message code="add.space"/>
					</tiles:putAttribute>	
				</tiles:insertDefinition>
			</tiles:putAttribute>
			<tiles:putAttribute name="body">
				<form:form modelAttribute="space">
					<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
					<div>
						<table class="basic-form">
							<tr>
								<td class="label"><label for="spaceId">Space ID</label></td>
								<td class="input">
									<form:input id="spaceId" path="spaceId" />
									<form:errors path="spaceId" cssClass="message-error" />
								</td>
								<td class="field-help">Space ID help goes here.</td>
							</tr>
							<tr>
								<td class="label"><label for="access">Space Access</label></td>
								<td class="input">
									<form:select path="access">
										<form:option value="OPEN" label="Open" />
										<form:option value="CLOSED" label="Closed" />
									</form:select>
									<form:errors path="access" cssClass="message-error" />
								</td>
								<td class="field-help">Access field description goes here.</td>
							</tr>
						</table>
					</div>
					<div class="basic-form-buttons" >
						<input type="submit" name="_eventId_submit" value="Add"/> 
						<input type="submit" name="_eventId_cancel" value="Cancel"/> 
					</div>
				</form:form>
			</tiles:putAttribute>
		</tiles:insertDefinition>
	</tiles:putAttribute>
</tiles:insertDefinition>

