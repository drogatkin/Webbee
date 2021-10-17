   function doSearch() {
	   var sv = getElement('search').value;
	   goto(formName+'?search='+encodeURIComponent(sv));
   }
   
   function checkEnter(e) {
	   var kc = e?(e.which?e.which:e.keyCode):event.keyCode;
	   if (kc == 13)
		   doSearch();
   }