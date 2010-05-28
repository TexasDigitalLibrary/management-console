<%@include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="app-frame" >
	<tiles:putAttribute name="title">
		<spring:message code="spaces" />	
	</tiles:putAttribute>
	<tiles:putAttribute name="header-extensions">
		<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/spaces-manager.js"></script>
	</tiles:putAttribute>

	<tiles:putAttribute name="main-content">
		<div class="center-north">
			<div style="float:left">
				<label for="providerList">Provider</label>
				<select name="providerList"><option>Amazon S3</option>
				</select>
			</div>
	
			<div style="float:left">
	        	<ul class="horizontal-list">
	        		<li>
			        	<input type="button" value="Browse"/>
			    	</li>
		       		<li>
			        	<input type="button" value="Search"/>
			    	</li>
	        	</ul>
	        </div>
	        <!-- 
	        FIX ME  - right padding seems to be necessary to render properly in firefox
	        			without it, there is an ugly horizontal scroll for no apparent 
	        			reason. -db
	        
	         -->
			<div style="float:right;padding-right:0.25em" >
				<span>
				4 Items
				<input type="button" value="+"/>   			
				<input type="button" value="^"/>   	
				</span>
	   					
	   		</div>
	   	</div>
	   	<div id="list-browser">
			<div id="spaces-list-view">
				<div class="north">
					<h5 class="header">
						Spaces
						<input style="float:right" class="add-space-button" type="button" value="Add Space"/>
					</h5>
					<div class="header">
						<span style="float:left"><input id="check-all-spaces" type="checkbox"/> prev next</span> <span style="float:right"><input type="text"/></span>
					</div>
				</div>
			
				<div class="center">
					<div class="dc-item-list" id="spaces-list">
						<div class="dc-item space-item prototype" style="display:none">
							<input  type="checkbox"/><span class="space-id">Space Id Here</span>
							<div class="dc-action-panel">
								<input type="button" value="+"/>
								<input type="button" value="-"/>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div id="content-item-list-view">
				<div class="north">
					<h5 class="header">
						Content Items
						<input style="float:right" class="add-content-item-button" type="button" value="Add Content Item"/>
					</h5>
					<div class="header" >
						<span style="float:left"><input type="checkbox"/> prev next</span> <span style="float:right"><input type="text"/></span>
					</div>
				</div>
			
				<div class="center">
					<div class="dc-item-list" id="contentItemList">
						<div class="dc-item">
							<input type="checkbox"/>content item name here
							<div class="dc-action-panel">
								<input type="button" value="+"/>
								<input type="button" value="-"/>
							</div>
						</div>
					</div>
				</div>
			</div>
	   	</div>
		<div id="detail-pane">
		</div>	
	
	<!-- 
	Space Detail Pane:  The div is invisible and used as a prototype for displaying specific space
						details.
	 -->
	<div id="spaceDetailPane" style="display:none">
		<div class="north">
			<h3 class="header"><img src="xxx" height="25" width="25" style="background-color:#DDD"/> Space Detail</h3>
			<h5 class="header object-name">Space Name Here</h5>
			<div class="header toggle-control">
				Access: 
				<input type="button" value="Open"/>
				<input type="button" value="Close"/>
			</div>
			<div class="header button-bar">
				<ul class="horizontal-list">
					<li>
						<input class="add-content-item-button" type="button" value="Add Content"/>
					</li>
					<li>
						<input type="button" value="Delete"/>
					</li>
				</ul>
			</div>

		</div>
		<div class="center">
			<div class="dc-expandable-panel">
					<h4>Details</h4>
					<table>
						<tr>
							<td>
								Items
							</td>
							<td>
								<span id="space-item-count">3</span>
							</td>
						</tr>
						<tr>
							<td>
								Created
							</td>
							<td>
								<span id="space-created-date">Fri, 09 Apr 2010 23:13:00 UTC</span>
							</td>
						</tr>
					</table>
			</div>
			<div id="metadata-panel" class="dc-expandable-panel">
					<h4>Metadata</h4>
					<table  width="100%">
						<tr class="dc-mouse-panel-activator">
							<td class="name">
								Lorem
							</td>
							<td class="value">
								Ipsum dolor								
								<span class="dc-mouse-panel">
									<input type="button"  value="x"/>
								</span>
							</td>
						</tr>
						<tr>
							<td class="name">
								<input type="text" value="" size="15"/>
							</td>
							<td class="value">
								<input type="text" value="" size="20"/>
								<input type="button" value="+"/>
							</td>
						</tr>
					</table>
			</div>
			<div id="tag-panel" class="dc-expandable-panel">
					<h4>Tags</h4>
					<table  width="100%">
						<tr>
							<td>
								<ul class="horizontal-list">
									<li class="dc-mouse-panel-activator">
										anicca
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									<li class="dc-mouse-panel-activator">
										dukkha
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									<li class="dc-mouse-panel-activator">
										anatta
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									
								</ul>
							</td>
						</tr>
						<tr>
							<td>
								<input type="text" value="" size="35"/>
								<input type="button" value="+"/>
							</td>
						</tr>
					</table>
			</div>

		</div>
	</div>

	<div id="genericDetailPane" style="display:none">
		<div class="north">
			Nothing is selected.
		</div>
		<div class="center">
		</div>
	</div>

	<div id="spaceMultiSelectPane" style="display:none">
		<div class="north">
			<h3 class="header"><img src="xxx" height="25" width="25" style="background-color:#DDD"/> Space Detail</h3>
			<h5 class="header object-name">Multiple Spaces Selected</h5>
			<div class="header toggle-control">
				Access: 
				<input type="button" value="Open"/>
				<input type="button" value="Close"/>
			</div>
			<div class="header button-bar">
				<ul class="horizontal-list">
					<li>
						<input type="button" value="Delete"/>
					</li>
				</ul>
			</div>
		</div>
		<div class="center">
			<div id="metadata-panel" class="dc-expandable-panel">
					<h4>Metadata</h4>
					<table  width="100%">
						<tr class="dc-mouse-panel-activator">
							<td class="name">
								Lorem
							</td>
							<td class="value">
								Ipsum dolor								
								<span class="dc-mouse-panel">
									<input type="button"  value="x"/>
								</span>
							</td>
						</tr>
						<tr>
							<td class="name">
								<input type="text" value="" size="15"/>
							</td>
							<td class="value">
								<input type="text" value="" size="20"/>
								<input type="button" value="+"/>
							</td>
						</tr>
					</table>
			</div>
			<div id="tag-panel" class="dc-expandable-panel">
					<h4>Tags</h4>
					<table  width="100%">
						<tr>
							<td>
								<ul class="horizontal-list">
									<li class="dc-mouse-panel-activator">
										anicca
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									<li class="dc-mouse-panel-activator">
										dukkha
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									<li class="dc-mouse-panel-activator">
										anatta
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									
								</ul>
							</td>
						</tr>
						<tr>
							<td>
								<input type="text" value="" size="35"/>
								<input type="button" value="+"/>
							</td>
						</tr>
					</table>
			</div>

		</div>
	</div>

	<!-- 
		an invisible  prototype for content items details.
	 -->
	<div id="contentItemDetailPane" style="display:none" >
		<div class="north">
			<h3 class="header"><img src="xxx" height="25" width="25" style="background-color:#DDD"/> Content Detail</h3>
			<h5 class="header object-name">Content Name</h5>
			<div class="header">
				<img src="xxx" height="50" width="50"/><span>Mime Type:</span><span>image/jpg</span>
			</div>
			<div class="header button-bar">
				<ul class="horizontal-list">
					<li>
						<input type="button" value="Edit"/>
					</li>
					<li>
						<input type="button" value="Download"/>
					</li>
					<li>
						<input type="button" value="Delete"/>
					</li>
				</ul>
			</div>

		</div>
		<div class="center">
			<div class="dc-expandable-panel">
					<h4>Preview</h4>
					<div style="text-align:center">
						<a href="xxx">
							<img src="xxx" height="200" width="200"/>					
						</a>
					</div>

			</div>
			<div class="dc-expandable-panel">
					<h4>Details</h4>
					<table>
						<tr>
							<td>
								Items
							</td>
							<td>
								<span id="space-item-count">3</span>
							</td>
						</tr>
						<tr>
							<td>
								Created
							</td>
							<td>
								<span id="space-created-date">Fri, 09 Apr 2010 23:13:00 UTC</span>
							</td>
						</tr>
					</table>
			</div>
			<div id="metadata-panel" class="dc-expandable-panel">
					<h4>Metadata</h4>
					<table  width="100%">
						<tr class="dc-mouse-panel-activator">
							<td class="name">
								Lorem
							</td>
							<td class="value">
								Ipsum dolor								
								<span class="dc-mouse-panel">
									<input type="button"  value="x"/>
								</span>
							</td>
						</tr>
						<tr>
							<td class="name">
								<input type="text" value="" size="15"/>
							</td>
							<td class="value">
								<input type="text" value="" size="20"/>
								<input type="button" value="+"/>
							</td>
						</tr>
					</table>
			</div>
			<div id="tag-panel" class="dc-expandable-panel">
					<h4>Tags</h4>
					<table  width="100%">
						<tr>
							<td>
								<ul class="horizontal-list">
									<li class="dc-mouse-panel-activator">
										anicca
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									<li class="dc-mouse-panel-activator">
										dukkha
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									<li class="dc-mouse-panel-activator">
										anatta
										<span class="dc-mouse-panel">
											<input type="button"  value="x"/>
										</span>
									</li>
									
								</ul>
							</td>
						</tr>
						<tr>
							<td>
								<input type="text" value="" size="35"/>
								<input type="button" value="+"/>
							</td>
						</tr>
					</table>
			</div>

		</div>
	</div>
	
	<div id="add-space-dialog" class="dc-dialog" title="Add Space">
		<p class="validateTips">All form fields are required.</p>
		<form>
		<fieldset>
			<label for="name">Name</label>
			<input type="text" name="name" id="name" class="text ui-widget-content ui-corner-all" />
		</fieldset>
		</form>

	</div>

	<div id="add-content-item-dialog" class="dc-dialog" title="Add Content Item">
		<form>
		<fieldset>
			<label for="name">Item Name</label>
			<input type="text" name="name" id="name" class="text ui-widget-content ui-corner-all" />
		</fieldset>
		</form>

	</div>
	</tiles:putAttribute>
</tiles:insertDefinition>

