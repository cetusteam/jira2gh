package com.efilogix.main;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class BuildIntermediateXMLFiles {

	public BuildIntermediateXMLFiles() {
		

		// document.addProcessingInstruction("qbxml", "version=\"6.0\"");
		// Element rootElement = document.addElement("QBXML");
		// Element signMsgElement = rootElement.addElement("SignonMsgsRq");
		// Element signonTicketRqElement = signMsgElement
		// .addElement("SignonTicketRq");
		// signonTicketRqElement.addElement("ClientDateTime").addText(
		// new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
		// .format(new Date()));
		// signonTicketRqElement.addElement("SessionTicket").addText(sesstkt);
		// signonTicketRqElement.addElement("Language").addText("English");
		// signonTicketRqElement.addElement("AppID").addText(
		// getQuickbooksParams().getAppId());
		// signonTicketRqElement.addElement("AppVer").addText("1");
		// Element qBXMLMsgsRqElement = rootElement.addElement("QBXMLMsgsRq");
		// qBXMLMsgsRqElement.addAttribute("onError", "stopOnError");
		// return qBXMLMsgsRqElement;
		
		
		
		/*******/
		
		// Document document = DocumentHelper.createDocument();
		// Element qBXMLMsgsRqElement = buildBaseXmlQuery(sesstkt, document);
		// Element customerQueryRqElement = qBXMLMsgsRqElement
		// .addElement("CustomerQueryRq");
		// customerQueryRqElement.addAttribute("requestID", "2");
		// customerQueryRqElement.addElement("ListID").addText(listId);
		// return document.asXML();
		
		
		/********/
		
//		String sesstkt = null;
//        try {
//            String sessResp = doRequest(rqAddr, request, "POST");
//            // Error to get the session response
//            if (StringUtils.isBlank(sessResp)) {
//                return null;
//            }
//            if (log.isDebugEnabled()) {
//                log.debug("sessResp :" + sessResp);
//            }
//            // Now we're ready to post our request.
//            SAXReader reader = buildReader();
//            Document document = reader.read(new StringReader(sessResp));
//            String dtStatusCode = document
//                    .valueOf("//QBXML/SignonMsgsRs/SignonDesktopRs/@statusCode");
//            if (dtStatusCode == null) {
//                throw new RuntimeException(
//                        "There is not SignonDesktopRs node at xml response or the status code is null");
//            }
//            log.debug("dtStatusCode " + dtStatusCode);
//            if (dtStatusCode.compareTo("0") != 0) { // error!
//                if (dtStatusCode.compareTo(SESSION_AUTH_REQ_NEW) == 0) {
//                    String errorMessage = "You have login security turn on QBOE.\n"
//                            + "Login Security prohibits automated access to QuickBooks Online Edition programatically  \n"
//                            + "Turn this off. \n"
//                            + "Go to QBOE Account --> My Account --> Connection List --> Choose Connection --> Edit --> \n"
//                            + "Choose No. Anyone who can run ... can use the connection.";
//                    throw new RuntimeException(errorMessage);
//                }
//                return null;
//            }
//            Node sessionTicketNode = document
//                    .selectSingleNode("//QBXML/SignonMsgsRs/SignonDesktopRs/SessionTicket");
//            if (sessionTicketNode == null) {
//                throw new RuntimeException(
//                        "There is not SessionTicket xml node when trying to get it from QuickBooks");
//            }
//            sesstkt = sessionTicketNode.getText();
//            if (StringUtils.isBlank(sesstkt)) {
//                throw new RuntimeException(
//                        "Session ticket is null in xml response from QuickBooks");
//            }
//        } catch (QBOEException e) {
//            throw new RuntimeException(e);
//        } catch (DocumentException e) {
//            throw new RuntimeException(e);
//        }

		
		
		
	}

}
