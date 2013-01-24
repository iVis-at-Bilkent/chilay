<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="1.2">
<jsp:directive.page import="javax.naming.OperationNotSupportedException"/>
<jsp:directive.page import="org.ivis.layout.Layout"/>
<jsp:directive.page import="java.io.FileOutputStream"/>
<jsp:directive.page import="org.ivis.layout.LGraphManager"/>
<jsp:directive.page import="java.io.BufferedOutputStream"/>
<jsp:directive.page import="java.io.BufferedInputStream"/>
<jsp:directive.page import="org.ivis.layout.cose.CoSELayout"/>
<jsp:directive.page import="org.ivis.layout.cise.CiSELayout"/>
<jsp:directive.page import="org.ivis.layout.LayoutConstants"/>
<jsp:directive.page import="org.ivis.layout.LayoutOptionsPack"/>
<jsp:directive.page import="java.util.List"/>
<jsp:directive.page import="java.io.FileInputStream"/>
<jsp:directive.page import="java.io.OutputStream"/>
<jsp:directive.page import="org.ivis.layout.LGraph"/>
<jsp:directive.page import="org.ivis.io.xml.XmlIOHandler"/>
<jsp:directive.page import="org.apache.commons.fileupload.FileItem"/>
<jsp:directive.page import="java.util.HashMap"/>
<jsp:directive.page import="java.io.File"/>
<jsp:directive.page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"/>
<jsp:directive.page import="org.apache.commons.fileupload.servlet.ServletFileUpload"/>
	<jsp:scriptlet><![CDATA[

	/**
	 This JSP page is responsible for retrieving posted xml files from client
	 side, layout them and return back to client.
	 @author Semih Ekiz
	 @author Ugur Dogrusoz
	 Copyright: Bilkent Center for Bioinformatics, Bilkent University, 2007 - present
	*/

	// Files can be uploaded only by multi-part content requests.
	if (ServletFileUpload.isMultipartContent(request))
	{
		// Initialize the mechanism for saving the posted file and allocate the
		// temp space for the file.
		
		ServletFileUpload servletFileUpload = new ServletFileUpload(
				new DiskFileItemFactory());
		servletFileUpload.setSizeMax(104857600); // 100 MB file size limit

		DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
		diskFileItemFactory.setSizeThreshold(104857600);

		File repositoryPath = new File("rep");
		diskFileItemFactory.setRepository(repositoryPath);

		// Retrieve the posted data
		List<FileItem> itemsList = servletFileUpload.parseRequest(request);

		// Iterate over each posted item
		FileItem fileItem = null;

		HashMap<String, String> options = new HashMap<String, String>();

		String layoutStyle = "";
		for (FileItem item : itemsList)
		{
			// Check whether the posted item is a file item.
			if (!item.isFormField())
			{
				fileItem = item;
			} 
			else
			{
				if (item.getFieldName().equals("layoutStyle"))
				{
					layoutStyle = item.getString();
				}

				options.put(item.getFieldName(), item.getString());
			}
		}

		// Find the name of the uploaded file.
		String xmlFileName = fileItem.getName();
		
		char folderSep = '/';
		if (xmlFileName.indexOf('/') < 0)
		{
			folderSep = '\\';
		}
		
		if (xmlFileName.indexOf(folderSep) >= 0)
		{
			xmlFileName = xmlFileName.substring(xmlFileName.lastIndexOf(folderSep),
				xmlFileName.lastIndexOf('.'));
		}
		
		File uploadedXmlFile = new File(xmlFileName+".xml");

		// Write the uploaded file to the place where it is supposed to be.
		fileItem.write(uploadedXmlFile);
		
		Layout layout;
		
		assert (layoutStyle.equals("cose") || layoutStyle.equals("cise")) :
			"Invalid layout style!";

		if (layoutStyle.equals("cose"))
		{
			layout = new CoSELayout();
		}
		else if (layoutStyle.equals("cise"))
		{
			layout = new CiSELayout();
		}
		else
		{
			throw new OperationNotSupportedException(
				"Given layout style ("+layoutStyle+") is not supported");
		}
		
		XmlIOHandler ioHandler = new XmlIOHandler(layout);

		FileInputStream is = new FileInputStream(new File(uploadedXmlFile.getAbsolutePath()));

		// Parse layout XML file to create necessary layout (l-level) model
		ioHandler.fromXML(is);

		// Create options pack and populate it with the posted parameters
		LayoutOptionsPack layoutOptionsPack = LayoutOptionsPack.getInstance();
		layoutOptionsPack.setDefaultLayoutProperties();

		String layoutQualityString = options.get("layoutQuality");

		if (layoutQualityString != null)
		{
			if (layoutQualityString.equals(LayoutConstants.DEFAULT_QUALITY))
			{
				layoutOptionsPack.getGeneral().
					setLayoutQuality(LayoutConstants.DEFAULT_QUALITY);
			}
			else if (layoutQualityString.equals(LayoutConstants.PROOF_QUALITY))
			{
				layoutOptionsPack.getGeneral().
					setLayoutQuality(LayoutConstants.PROOF_QUALITY);
			}
			else
			{
				layoutOptionsPack.getGeneral().
					setLayoutQuality(LayoutConstants.DRAFT_QUALITY);
			}
		}

		String animateOnLayout = options.get("animateOnLayout");

		if (animateOnLayout != null)
		{
			layoutOptionsPack.getGeneral().setAnimationOnLayout(
				(Boolean.parseBoolean(animateOnLayout)));
		}

		String incremental = options.get("incremental");

		if (incremental != null)
		{
			layoutOptionsPack.getGeneral().
				setIncremental((Boolean.parseBoolean(incremental)));
		}

//		String uniformNodeSize = options.get("uniformNodeSize");
//
//		if (uniformNodeSize != null)
//		{
//			layoutOptionsPack.getGeneral().
//				setUniformNodeSize((Boolean.parseBoolean(uniformNodeSize)));
//		}

		if (layoutStyle.equals("cose"))
		{
			String springStrength = options.get("springStrength");

			if (springStrength != null)
			{
				layoutOptionsPack.getCoSE().
					setSpringStrength((Integer.parseInt(springStrength)));
			}

			String repulsionStrength = options.get("repulsionStrength");

			if (repulsionStrength != null)
			{
				layoutOptionsPack.getCoSE().
					setRepulsionStrength((Integer.parseInt(repulsionStrength)));
			}

			String gravityStrentgh = options.get("gravityStrength");

			if (gravityStrentgh != null)
			{
				layoutOptionsPack.getCoSE().
					setGravityStrength((Integer.parseInt(gravityStrentgh)));
			}

			String compoundGravityStrentgh =
				options.get("compoundGravityStrength");

			if (compoundGravityStrentgh != null)
			{
				layoutOptionsPack.getCoSE().setCompoundGravityStrength(
					(Integer.parseInt(compoundGravityStrentgh)));
			}

			String idealEdgeLength = options.get("idealEdgeLength");

			if (idealEdgeLength != null)
			{
				layoutOptionsPack.getCoSE().
					setIdealEdgeLength((Integer.parseInt(idealEdgeLength)));
			}
		}
		else if (layoutStyle.equals("cise"))
		{
			String nodeSeparation = options.get("nodeSeparation");

			if (nodeSeparation != null)
			{
				layoutOptionsPack.getCiSE().
					setNodeSeparation((Integer.parseInt(nodeSeparation)));
			}

			String desiredEdgeLength = options.get("desiredEdgeLength");

			if (desiredEdgeLength != null)
			{
				layoutOptionsPack.getCiSE().
					setDesiredEdgeLength((Integer.parseInt(desiredEdgeLength)));
			}

			String interClusterEdgeLengthFactor =
				options.get("interClusterEdgeLengthFactor");

			if (interClusterEdgeLengthFactor != null)
			{
				layoutOptionsPack.getCiSE().setInterClusterEdgeLengthFactor(
					(Integer.parseInt(interClusterEdgeLengthFactor)));
			}
		}

		// Apply layout
		layout.runLayout();
		
		File laidOutXmlFile = new File(xmlFileName+"_layout.xml");
		FileOutputStream os = new FileOutputStream(new File(laidOutXmlFile.getAbsolutePath()));
		ioHandler.toXML(os);	
		
		// Prepare the response headers
		response.setContentType("application/xml");
		response.setHeader("Content-disposition",
			"attachment; filename="+laidOutXmlFile.getName());
		response.setContentLength((int) laidOutXmlFile.length());
		
		// Attach the file to the response
		BufferedInputStream fis = 
			new BufferedInputStream(new FileInputStream(laidOutXmlFile));
		BufferedOutputStream responseOutputStream = 
			new BufferedOutputStream(response.getOutputStream());

		int read = fis.read();
		while (read >= 0)
		{
			responseOutputStream.write((byte) read);
			read = fis.read();
		}
		responseOutputStream.flush();

		responseOutputStream.close();
	}]]></jsp:scriptlet>
</jsp:root>