/**
 * Model for DIDL-Lite content
 * @author Christian Gawron
 *
 */

// xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/" 
//   xmlns:dc="http://purl.org/dc/elements/1.1/" 
//   xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/"

@javax.xml.bind.annotation.XmlSchema(
namespace = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/",
//location = "http://www.upnp.org/schemas/av/didl-lite-v2.xsd",
elementFormDefault = XmlNsForm.QUALIFIED
/* ,
 xmlns = {
 @javax.xml.bind.annotation.XmlNs(prefix = "dc",
 namespaceURI = "http://purl.org/dc/elements/1.1/"),

 @javax.xml.bind.annotation.XmlNs(prefix = "upnp",
 namespaceURI = "urn:schemas-upnp-org:metadata-1-0/upnp/")
 }*/)
package de.cgawron.mp3.server.upnp.model;

import javax.xml.bind.annotation.XmlNsForm;

