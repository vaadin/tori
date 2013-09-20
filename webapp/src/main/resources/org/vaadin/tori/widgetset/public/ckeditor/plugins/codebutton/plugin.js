(function(){
	CKEDITOR.plugins.add( 'codebutton', {
	    init: function( editor ) {
	    	var codeStyle = new CKEDITOR.style( { element: 'code' } );
	    	
	    	 editor.addCommand( 'sourcecode', new CKEDITOR.styleCommand( codeStyle ) );
	    	 
	         editor.ui.addButton( 'codebutton', {
	             label: 'Code',
	             command: 'sourcecode',
	             icon: 'plugins/codebutton/icon.png'
	         });
	    }
	});
})();