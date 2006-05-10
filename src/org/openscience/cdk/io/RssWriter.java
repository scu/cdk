package org.openscience.cdk.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ProcessingInstruction;
import nu.xom.Text;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.SetOfAtomContainers;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.interfaces.ICrystal;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.ISetOfAtomContainers;
import org.openscience.cdk.interfaces.ISetOfMolecules;
import org.openscience.cdk.interfaces.ISetOfReactions;
import org.openscience.cdk.io.formats.CMLRSSFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.libio.cml.Convertor;

/**
 * Generatas an rss feed. It the object is a SetOfMolecules, the molecules
 * are put in separtly. All other objects a made cml and put in.
 *
 * @cdk.module       libio-cml
 * @cdk.builddepends xom-1.0.jar
 *
 * @author Stefan Kuhn
 *
 * @cdk.keyword RSS
 */

public class RssWriter extends DefaultChemObjectWriter {
	
	private final static String NS_RSS10      = "http://purl.org/rss/1.0/";
	private final static String NS_RDF        = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final static String NS_DCELEMENTS = "http://purl.org/dc/elements/1.1/";
	
    static BufferedWriter writer;
    private Map linkmap=new HashMap();
    private Map datemap=new HashMap();
    private Map titlemap=new HashMap();
    private Map creatormap=new HashMap();
    private String creator="";
    private String title="";
    private String link="";
    private String description="";
    private String publisher="";
    private String imagelink="";
    private String about="";
    private String timezone="+01:00";
    private Map multiMap=new HashMap();

    /**
     * Flushes the output and closes this object.
     */
    public void close() throws IOException {
        writer.close();
    }
    
    public IResourceFormat getFormat() {
        return new CMLRSSFormat();
    }
    
    
    public void setWriter(Writer out) throws CDKException {
    	if (out instanceof BufferedWriter) {
            writer = (BufferedWriter)out;
        } else {
            writer = new BufferedWriter(out);
        }
    }

    public void setWriter(OutputStream output) throws CDKException {
    	setWriter(new OutputStreamWriter(output));
    }
    
	public boolean accepts(Class classObject) {
		return true;
	}

    /**
     * Writes a IChemObject to the MDL molfile formated output. 
     *
     * @param object Best choice is a set of molecules
     */
	/* (non-Javadoc)
	 * @see org.openscience.cdk.io.IChemObjectWriter#write(org.openscience.cdk.interfaces.IChemObject)
	 */
	public void write(IChemObject object) throws CDKException {
		try{
		    ProcessingInstruction pi=new ProcessingInstruction("xml-stylesheet", "href=\"http://www.w3.org/2000/08/w3c-synd/style.css\" type=\"text/css\"");
		    Element rdfElement = new Element("rdf:RDF",NS_RDF);
		    rdfElement.addNamespaceDeclaration("",NS_RSS10);
		    rdfElement.addNamespaceDeclaration("mn","http://usefulinc.com/rss/manifest/");
		    rdfElement.addNamespaceDeclaration("dc",NS_DCELEMENTS);
		    rdfElement.addNamespaceDeclaration("cml",Convertor.NS_CML);
		    Document doc = new Document(rdfElement);
		    doc.insertChild(pi,0);
		    Element channelElement = new Element("channel",NS_RSS10);
		    Element titleElement = new Element("title",NS_RSS10);
		    titleElement.appendChild(new Text(title));
		    channelElement.appendChild(titleElement);
		    Element linkElement = new Element("link",NS_RSS10);
		    linkElement.appendChild(new Text(link));
		    channelElement.appendChild(linkElement);
		    Element descriptionElement = new Element("description",NS_RSS10);
		    descriptionElement.appendChild(new Text(description));
		    channelElement.appendChild(descriptionElement);
		    Element publisherElement = new Element("dc:publisher",NS_DCELEMENTS);
		    publisherElement.appendChild(new Text(publisher));
		    channelElement.appendChild(publisherElement);
		    Element creatorElement = new Element("dc:creator",NS_DCELEMENTS);
		    creatorElement.appendChild(new Text(creator));
		    channelElement.appendChild(creatorElement);
		    Element imageElement = new Element("image",NS_RSS10);
		    imageElement.addAttribute(new Attribute("rdf:resource",NS_RDF,imagelink));
		    channelElement.appendChild(imageElement);
		    Element itemsElement = new Element("items",NS_RSS10);
		    Element seqElement = new Element("rdf:Seq",NS_RDF);
		    itemsElement.appendChild(seqElement);
		    channelElement.appendChild(itemsElement);
		    channelElement.addAttribute(new Attribute("rdf:about",NS_RDF,about));
		    rdfElement.appendChild(channelElement);
		    List l=new Vector();
		    if(object instanceof ISetOfAtomContainers){
		    	for(int i=0;i<((SetOfAtomContainers)object).getAtomContainerCount();i++){
		    		l.add(((SetOfAtomContainers)object).getAtomContainer(i));
		    	}
		    }else{
		    	l.add(object);
		    }        	
		    for(int i=0;i<l.size();i++){
		      ChemObject co=(ChemObject)l.get(i);
		      Element itemElement = new Element("item",NS_RSS10);
		      String easylink=(String)linkmap.get(co);
		      if(easylink!=null)
		    	  itemElement.addAttribute(new Attribute("rdf:about",NS_RDF,easylink));
		      Element link2Element = new Element("link",NS_RSS10);
		      link2Element.appendChild(new Text(easylink));
		      itemElement.appendChild(link2Element);          
		      String title=(String)co.getProperties().get(CDKConstants.TITLE);
		      if(titlemap.get(co)!=null){
			      Element title2Element = new Element("title",NS_RSS10);
			      title2Element.appendChild(new Text((String)titlemap.get(co)));
			      itemElement.appendChild(title2Element);
		      }
		      if(title!=null){
		    	  Element description2Element = new Element("description",NS_RSS10);
		    	  description2Element.appendChild(new Text(title));
		    	  itemElement.appendChild(description2Element);
		          Element subjectElement = new Element("dc:subject",NS_DCELEMENTS);
		          subjectElement.appendChild(new Text(title));
		          itemElement.appendChild(subjectElement);
		      }
		      if(datemap.get(co)!=null){
			      Element dateElement = new Element("dc:date",NS_DCELEMENTS);
			      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
			      dateElement.appendChild(new Text(formatter.format((Date)datemap.get(co))+timezone));
			      itemElement.appendChild(dateElement);
		      }
		      Element creator2Element =new Element("dc:creator",NS_DCELEMENTS);
		      creator2Element.appendChild(new Text((String)creatormap.get(co)));
		      itemElement.appendChild(creator2Element);
		      Element root=null;
		      Convertor convertor=new Convertor(true,null);
		      object=(IChemObject)l.get(i);
		      if (object instanceof IMolecule) {
		      	root = convertor.cdkMoleculeToCMLMolecule((IMolecule)object);
		      }else if (object instanceof IAtomContainer) {
			     root = convertor.cdkAtomContainerToCMLMolecule((IAtomContainer)object);
			  } else if (object instanceof ICrystal) {
		      	root = convertor.cdkCrystalToCMLMolecule((ICrystal)object);
		      } else if (object instanceof IAtom) {
		      	root = convertor.cdkAtomToCMLAtom((IAtom)object);
		      } else if (object instanceof IBond) {
		      	root = convertor.cdkBondToCMLBond((IBond)object);
		      } else if (object instanceof IReaction) {
		      	root = convertor.cdkReactionToCMLReaction((IReaction)object);
		      } else if (object instanceof ISetOfReactions) {
		      	root = convertor.cdkSetOfReactionsToCMLReactionList((ISetOfReactions)object);
		      } else if (object instanceof ISetOfMolecules) {
		      	root = convertor.cdkSetOfMoleculesToCMLList((ISetOfMolecules)object);
		      } else if (object instanceof IChemSequence) {
		      	root = convertor.cdkChemSequenceToCMLList((IChemSequence)object);
		      } else if (object instanceof IChemModel) {
		      	root = convertor.cdkChemModelToCMLList((IChemModel)object);
		      } else if (object instanceof IChemFile) {
		      	root = convertor.cdkChemFileToCMLList((IChemFile)object);
		      } else {
		    	  throw new CDKException("Unsupported chemObject: " + object.getClass().getName());
		      }
		      itemElement.appendChild(root);
		      if(multiMap.get(co)!=null){
		    	  Collection coll=(Collection)multiMap.get(co);
		    	  Iterator it=coll.iterator();
		    	  while(it.hasNext()){
		    		 itemElement.appendChild((Element)it.next());
		    	  }
		      }
		      rdfElement.appendChild(itemElement);
		      Element imageElement2 = new Element("rdf:li",NS_RDF);
		      imageElement2.addAttribute(new Attribute("rdf:resource",NS_RDF,(String)linkmap.get(co)));
		      seqElement.appendChild(imageElement2);
		    }
	      writer.write(doc.toXML());
	      writer.flush();
		}catch(IOException ex){
			throw new CDKException(ex.getMessage());
		}

	}

	/**
	 * @return the datemap. If you put a java.util.Date in this map with one of the objects you want to write as key, it will be added as a date to this object (no validity check is done)
	 */
	public Map getDatemap() {
		return datemap;
	}

	/**
	 * @param datemap the datemap. If you put a java.uitl.Date in this map with one of the objects you want to write as key, it will be added as a datek to this object (no validity check is done)
	 */
	public void setDatemap(Map datemap) {
		this.datemap = datemap;
	}

	/**
	 * @return the linkmap. If you put a String in this map with one of the objects you want to write as key, it will be added as a link to this object (no validity check is done)
	 */
	public Map getLinkmap() {
		return linkmap;
	}

	/**
	 * @param linkmap the linkmap. If you put a String in this map with one of the objects you want to write as key, it will be added as a link to this object (no validity check is done)
	 */
	public void setLinkmap(Map linkmap) {
		this.linkmap = linkmap;
	}

	/**
	 * @return the titlemap. If you put a String in this map with one of the objects you want to write as key, it will be added as a title to this object (no validity check is done)
	 */
	public Map getTitlemap() {
		return titlemap;
	}

	/**
	 * @param titlemap the titlemap. If you put a String in this map with one of the objects you want to write as key, it will be added as a titel to this object (no validity check is done)
	 */
	public void setTitlemap(Map titlemap) {
		this.titlemap = titlemap;
	}

	/**
	 * @return the creatoremap. If you put a String in this map with one of the objects you want to write as key, it will be added as a creator to this object (no validity check is done)
	 */
	public Map getCreatormap() {
		return creatormap;
	}

	/**
	 * @param creatormap the creatormap. If you put a String in this map with one of the objects you want to write as key, it will be added as a creator to this object (no validity check is done)
	 */
	public void setCreatormap(Map creatormap) {
		this.creatormap = creatormap;
	}

	/**
	 * @param about This will be the about for the rss feed
	 */
	public void setAbout(String about) {
		this.about = about;
	}

	/**
	 * @param creator This will be the creator for the rss feed
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @param description This will be the description for the rss feed
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param imagelinkt This will be the imagelink for the rss feed
	 */
	public void setImagelink(String imagelink) {
		this.imagelink = imagelink;
	}

	/**
	 * @param link This will be the link for the rss feed
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * @param publisher This will be the publisher for the rss feed
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * @param title This will be the title for the rss feed
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the multimap. If you put any number of nu.xom.Elements in this map with one of the objects you want to write as key, it will be added as a child to the same node as the cml code of the object
	 */
	public Map getMultiMap() {
		return multiMap;
	}

	/**
	 * @param multiMap. If you put any number of nu.xom.Elements in this map with one of the objects you want to write as key, it will be added as a child to the same node as the cml code of the object
	 */
	public void setMultiMap(Map multiMap) {
		this.multiMap = multiMap;
	}

	/**
	 * @param timezone This will be added to the data as timezone. format according to 23c. Examples "+01:00" "-05:00"
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

}
