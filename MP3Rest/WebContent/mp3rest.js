  function getTrackRow(url) {
       	var row = new Element('tr');
    	new Ajax.Request(url, {
			method : 'get',
			requestHeaders : ['Accept', 'application/json'],
			evalJS : false,
			evalJSON : false,
			onSuccess : function(transport) {
				try {
					var track = transport.responseText.evalJSON();				 
			    	row.appendChild(new Element('td').update(track.trackNo));			    
			    	row.appendChild(new Element('td').appendChild(new Element('a', { href : track.self }).update(track.title)));
				} catch (e) {
					alert("caught: " + e.message);
				}
			},
			onFailure : function(transport) {
				alert("onFailure");
			},
			onException : function(request, exception) {
				alert("onException");
			}
		});

    	return row;
    }

	function showAlbum(id)
	{
		// alert("showAlbum " + id);
		new Ajax.Request('mp3/album/' + id, {
			method : 'get',
			requestHeaders : ['Accept', 'application/json'],
			evalJS : false,
			evalJSON : false,
			onSuccess : function(transport) {
				try {
					var album = transport.responseText.evalJSON();
					$('albumTitle').update(album.title);
					$('trackList').update("<tbody></tbody>");
					for (i=0; i<album.tracks.length; i++) {
						var row = getTrackRow(album.tracks[i]);
						$('trackList').appendChild(row);
					}
				} catch (e) {
					alert("caught: " + e.message);
				}
			},
			onFailure : function(transport) {
				alert("onFailure");
			},
			onException : function(request, exception) {
				alert("onException");
			}
		});
	}
	
	function initRenderer() {
		new Ajax.Request('rest/upnp/renderer', {
			method : 'get',
			requestHeaders : ['Accept', 'application/json'],
			evalJS : false,
			evalJSON : false,
			onSuccess : function(transport) {
				try {
					var renderers = transport.responseText.evalJSON();
					var table = $('renderer');
					for (i = 0; i < renderers.length; i++) {
						var row = new Element('tr');
						var tdTitle = document.createElement('td');
						var link = new Element('a', { href : 'javascript:showRenderer("' + renderers[i].rendererId +'")' }).update(renderers[i].name);
						tdTitle.appendChild(link);
						row.appendChild(tdTitle);
						table.appendChild(row);			
					}
				} catch (e) {
					alert("caught: " + e.message);
				}
			},
			onFailure : function(transport) {
				alert("onFailure");
			},
			onException : function(request, exception) {
				alert("onException");
			}
		});
	}