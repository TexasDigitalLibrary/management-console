<%@include file="/WEB-INF/jsp/include.jsp" %>


<div id="top-header">
	<img alt="<spring:message code="application.title"/>" height="25px" src="images/duraspace-logo.jpg">
	<strong>
		<spring:message code="application.title"/>
	</strong>
</div>

<tiles:importAttribute name="mainMenu" />

<table id="main-menu" >
	<tr>
		<td id="main-menu-left">
			<ul id="menu">
				<c:forEach items="${mainMenu}" var="mi">
					<li>
						<a href='<c:url value="${mi.href}" />' class="<c:if test="${mi.name == mainTab}">current</c:if>" >
							<spring:message code="${mi.messageKey}"/>
						</a>
					</li>
				</c:forEach>
			</ul>
		</td>		
		<td id="main-menu-center">
			<c:if test="${not empty flashMessage}">
				<span class="message-${fn:toLowerCase(flashMessage.severity)}">${flashMessage.text}</span>
			</c:if>
		</td>
		
		
		<td id="main-menu-right">
			<tiles:importAttribute name="contentStoreSelector" />

			<form>
				Providers:
				<select >
					<c:forEach var="store" items="${contentStoreSelector.contentStores}">
						<option value="${store.storeId}" selected="<c:if test="${store.storeId == contentStoreSelector.selectedId}">selected</c:if>">
							${store.storageProviderType}
						</option>
					</c:forEach>
				</select>
			</form>
		</td>
	</tr>
</table>
	