window.org_vaadin_tori_breadcrumbslayout_makesurethecrumbsfit = function() {
	var table = document.querySelector(".breadcrumbs .breadcrumbs-wrapper");
	var category = document.querySelector("div[location='category'] .v-link");
	var thread = document.querySelector("div[location='thread'] .v-link");
	
	if (category == null && thread == null) {
		return;
	}
	
	// undo any style changes made by our script
	if (category != null) {
		category.style.width = "";
	}
	
	if (thread != null) {
		thread.style.width = "";
	}
	
	var tableWidth = table.offsetWidth;
	var tableParent = table.parentNode;
	while (tableParent != null) {
		if (tableParent.offsetWidth < tableWidth) {
			table.style.width = tableParent.offsetWidth+"px";
			break;
		}
		tableParent = tableParent.parentNode;
	}
	
	// how much space do we have to work with
	var freeWidth = document.querySelector(".breadcrumbs-layout").offsetWidth;

	var cells = document.querySelector("#breadcrumbsrow").children;
	for (var i = 0; i < cells.length; i++) {
		// check if we actually need to do any adjustments?
		freeWidth -= cells[i].offsetWidth;
	}

	if (freeWidth < 0) {
		// some things are out of view - squish the crumbs. 
		
		if (category != null) {
			freeWidth += category.offsetWidth;
		}
		
		if (thread != null) {
			freeWidth += thread.offsetWidth;
		}
		
		var categoryNeedsAdjustment = (category != null) && (category.offsetWidth > freeWidth/2);
		var threadNeedsAdjustment = (thread != null) && (thread.offsetWidth > freeWidth/2);
		
		if (categoryNeedsAdjustment && threadNeedsAdjustment) {
			category.style.width = (freeWidth/2) + "px";
			thread.style.width = (freeWidth/2) + "px";
		} else if (categoryNeedsAdjustment) {
			freeWidth -= thread.offsetWidth;
			category.style.width = freeWidth + "px";
		} else {
			freeWidth -= category.offsetWidth;
			thread.style.width = freeWidth + "px";
		}
	}
	
	table.style.width = "";
};