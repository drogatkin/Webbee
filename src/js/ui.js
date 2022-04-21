var handlerUrl

function updateUI() {
	if (!handlerUrl || handlerUrl == '')
		handlerUrl = './ajax/Asyncupdate'
	
	var request = new XMLHttpRequest()
	request.open("GET", handlerUrl, true)
	request.setRequestHeader("Content-Type", "application/x-javascript;")
	//if (pageMark && pageMark != '')
	//	request.setRequestHeader("page-mark", pageMark);
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			if (request.status == 200) {
				if (request.responseText) {
					//alert(request.responseText);
					if (request.responseText == '')
						return;
					var uievents = null
					try {
						uievents = new Function("return "
							+ request.responseText)()
					} catch(e) {
						// can't parse
						return;
					}
					for ( var i in uievents) {
						var uie = uievents[i]
						//alert(typeof uie.eventHandler + uie.eventHandler);
						//if (typeof uie.eventHandler == 'function')
						if (window[uie.eventHandler])
							try {
								window[uie.eventHandler].apply(this, uie.parameters || [])
							} catch (e) {
								alert('WebBee: an exception at calling '+uie.eventHandler+' event handler '+e.name+': '+e.message);
							}
					}
				}
				
			} else if (request.status == 403 || request.status == 404 || request.status === 0 ||
			    request.status >= 500) 
				return
			updateUI()
		}	
	}
	request.send(null)
}

function releaseUI() {
	makeGenericAjaxCall(handlerUrl+'/../Completepending', '', true, 
		     function(res) {
		         if(res == 'ok')
		         ;
		     });
}