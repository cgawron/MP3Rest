  function getTrackRow(url) {
       	var row = new Element('tr');
       	var trackNo = row.appendChild(new Element('td'));
       	var title = row.appendChild(new Element('td'));
    	new Ajax.Request(url, {
			method : 'get',
			requestHeaders : ['Accept', 'application/json'],
			evalJS : false,
			evalJSON : false,
			onSuccess : function(transport) {
				try {
					var track = transport.responseText.evalJSON();				 
			    	trackNo.update(track.trackNo);			    
			    	title.appendChild(new Element('a', { href : track.self }).update(track.title));
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
		new Ajax.Request('rest/album/' + id, {
			method : 'get',
			requestHeaders : ['Accept', 'application/json'],
			evalJS : false,
			evalJSON : false,
			onSuccess : function(transport) {
				try {
					var album = transport.responseText.evalJSON();
					$('albumTitle').update(album.title);
					$('trackList').update("<tbody></tbody>");
					for (var i=0; i<album.tracks.length; i++) {
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

	function showRenderer(id)
	{
		alert("selected: " + id);
		new Ajax.Request('rest/upnp/renderer/' + id + '/state', {
			method : 'get',
			requestHeaders : ['Accept', 'application/json'],
			evalJS : false,
			evalJSON : false,
			onSuccess : function(transport) {
				try {
					var state = transport.responseText.evalJSON();
					alert(state);
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

	function initAlbum() {
		new Ajax.Request('rest/album', {
			method : 'get',
			requestHeaders : ['Accept', 'application/json'],
			evalJS : false,
			evalJSON : false,
			onSuccess : function(transport) {
				try {
					var albums = transport.responseText.evalJSON();
					var table = $('albums');
					for (var i = 0; i < albums.length; i++) {
						var row = new Element('tr');
						var tdTitle = document.createElement('td');
						var link = new Element('a', { href : 'javascript:showAlbum("' + albums[i].albumId +'")' }).update(albums[i].title);
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
	
	function initRenderer() {
		new Ajax.Request('rest/upnp/renderer', {
			method : 'get',
			requestHeaders : ['Accept', 'application/json'],
			evalJS : false,
			evalJSON : false,
			onSuccess : function(transport) {
				try {
					var renderers = transport.responseText.evalJSON();
					var datalist = $('renderers');
					for (var i = 0; i < renderers.length; i++) {
						var option = new Element('option', { "value" : renderers[i].identifier, "label" : renderers[i].name });
						// var link = new Element('a', { href : 'javascript:showRenderer("' + renderers[i].rendererId +'")' }).update(renderers[i].name);

						datalist.appendChild(option);			
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