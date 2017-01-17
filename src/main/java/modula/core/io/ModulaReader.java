package modula.core.io;

import modula.core.env.URLResolver;
import modula.core.model.CustomAction;
import modula.core.model.ModelException;
import modula.core.model.Modula;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import javax.xml.stream.*;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.*;

/**
 * @description: 用于转换Modula文档到Modula Java对象
 * @author: gubing.gb
 * @date: 2017/1/1.
 */
public final class ModulaReader {
    private static final String MODULA_REQUIRED_VERSION = "1.0";

    //---- ERROR MESSAGES ----//
    /**
     * 空URL
     */
    private static final String ERR_NULL_URL = "Cannot parse null URL";

    /**
     * 空PATH
     */
    private static final String ERR_NULL_PATH = "Cannot parse null path";

    /**
     * 空InputStream
     */
    private static final String ERR_NULL_ISTR = "Cannot parse null InputStream";

    /**
     * 空Reader
     */
    private static final String ERR_NULL_READ = "Cannot parse null Reader";

    /**
     * 空Source
     */
    private static final String ERR_NULL_SRC = "Cannot parse null Source";

    /**
     * Action定义错误
     */
    private static final String ERR_CUSTOM_ACTION_TYPE = "Custom actions list"
            + " contained unknown object, class not a Commons Modula Action class subtype: ";

    /**
     * 解析DOM树错误
     */
    private static final String ERR_PARSER_CFG = "ParserConfigurationException while trying"
            + " to parse stream into DOM node(s).";

    /**
     * 找不到定义文档
     */
    private static final String ERR_STATE_SRC =
            "Source attribute in <state src=\"{0}\"> cannot be parsed";

    /**
     * 未知的state
     */
    private static final String ERR_STATE_SRC_FRAGMENT = "URI Fragment in "
            + "<state src=\"{0}\"> is an unknown state in referenced document";

    /**
     * state的src指向非state或final
     */
    private static final String ERR_STATE_SRC_FRAGMENT_TARGET = "URI Fragment"
            + " in <state src=\"{0}\"> does not point to a <state> or <final>";

    /**
     *
     */
    private static final String ERR_REQUIRED_ATTRIBUTE_MISSING = "<{0}> is missing"
            + " required attribute \"{1}\" value at {2}";

    /**
     * attribute属性类型错误
     */
    private static final String ERR_ATTRIBUTE_NOT_BOOLEAN = "Illegal value \"{0}\""
            + "for attribute \"{1}\" in element <{2}> at {3}."
            + " Only the value \"true\" or \"false\" is allowed.";

    /**
     * 前缀错误
     */
    private static final String ERR_RESERVED_ID_PREFIX = "Reserved id prefix \""
            + Modula.GENERATED_TT_ID_PREFIX + "\" used for <{0} id=\"{1}\"> at {2}";

    /**
     * state Target错误
     */
    private static final String ERR_UNSUPPORTED_TRANSITION_TYPE = "Unsupported transition type "
            + "for <transition type=\"{0}\"> at {1}.";

    /**
     * state Target不是state或final
     */
    private static final String ERR_INVALID_VERSION = "The <modula> element defines"
            + " an unsupported version \"{0}\", only version \"1.0\" is supported.";

    //--------------------------- XML VOCABULARY ---------------------------//
    //---- ELEMENT NAMES ----//
    private static final String ELEM_ASSIGN = "assign";
    private static final String ELEM_CANCEL = "cancel";
    private static final String ELEM_CONTENT = "content";
    private static final String ELEM_DATA = "data";
    private static final String ELEM_DATAMODEL = "datamodel";
    private static final String ELEM_ELSE = "else";
    private static final String ELEM_ELSEIF = "elseif";
    private static final String ELEM_RAISE = "raise";
    private static final String ELEM_FINAL = "final";
    private static final String ELEM_FINALIZE = "finalize";
    private static final String ELEM_HISTORY = "history";
    private static final String ELEM_IF = "if";
    private static final String ELEM_INITIAL = "initial";
    private static final String ELEM_INVOKE = "invoke";
    private static final String ELEM_FOREACH = "foreach";
    private static final String ELEM_LOG = "log";
    private static final String ELEM_ONENTRY = "onentry";
    private static final String ELEM_ONEXIT = "onexit";
    private static final String ELEM_PARALLEL = "parallel";
    private static final String ELEM_PARAM = "param";
    private static final String ELEM_SCRIPT = "script";
    private static final String ELEM_Modula = "modula";
    private static final String ELEM_SEND = "send";
    private static final String ELEM_STATE = "state";
    private static final String ELEM_TRANSITION = "transition";
    private static final String ELEM_VAR = "var";

    //---- ATTRIBUTE NAMES ----//
    private static final String ATTR_ARRAY = "array";
    private static final String ATTR_AUTOFORWARD = "autoforward";
    private static final String ATTR_COND = "cond";
    private static final String ATTR_DELAY = "delay";
    private static final String ATTR_EVENT = "event";
    private static final String ATTR_EXMODE = "exmode";
    private static final String ATTR_EXPR = "expr";
    private static final String ATTR_HINTS = "hints";
    private static final String ATTR_ID = "id";
    private static final String ATTR_INDEX = "index";
    private static final String ATTR_INITIAL = "initial";
    private static final String ATTR_ITEM = "item";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_LOCATION = "location";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_NAMELIST = "namelist";
    private static final String ATTR_PROFILE = "profile";
    private static final String ATTR_SENDID = "sendid";
    private static final String ATTR_SRC = "src";
    private static final String ATTR_SRCEXPR = "srcexpr";
    private static final String ATTR_TARGET = "target";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_VERSION = "version";

    //------------------------- PUBLIC API METHODS -------------------------//
    /*
     * Public methods
     */

    /**
     * Parse the Modula document at the supplied path.
     *
     * @param modulaPath The real path to the Modula document.
     * @return The parsed output, the Commons Modula object model corresponding to the Modula document.
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static Modula read(final String modulaPath)
            throws IOException, ModelException, XMLStreamException {

        return read(modulaPath, new Configuration());
    }

    /**
     * Parse the Modula document at the supplied path with the given {@link Configuration}.
     *
     * @param modulaPath    The real path to the Modula document.
     * @param configuration The {@link Configuration} to use when parsing the Modula document.
     * @return The parsed output, the Commons Modula object model corresponding to the Modula document.
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static Modula read(final String modulaPath, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (modulaPath == null) {
            throw new IllegalArgumentException(ERR_NULL_PATH);
        }
        Modula modula = readInternal(configuration, null, modulaPath, null, null, null);
        if (modula != null) {
            //ModelUpdater.updateModula(modula);
        }
        return modula;
    }

    /**
     * Parse the Modula document at the supplied {@link URL}.
     *
     * @param modulaURL The Modula document {@link URL} to parse.
     * @return The parsed output, the Commons Modula object model corresponding to the Modula document.
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static Modula read(final URL modulaURL)
            throws IOException, ModelException, XMLStreamException {

        return read(modulaURL, new Configuration());
    }

    /**
     * Parse the Modula document at the supplied {@link URL} with the given {@link Configuration}.
     *
     * @param modulaURL     The Modula document {@link URL} to parse.
     * @param configuration The {@link Configuration} to use when parsing the Modula document.
     * @return The parsed output, the Commons Modula object model corresponding to the Modula document.
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static Modula read(final URL modulaURL, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (modulaURL == null) {
            throw new IllegalArgumentException(ERR_NULL_URL);
        }
        Modula modula = readInternal(configuration, modulaURL, null, null, null, null);
        if (modula != null) {
            //ModelUpdater.updateModula(modula);
        }
        return modula;
    }

    /**
     * Parse the Modula document supplied by the given {@link InputStream}.
     *
     * @param modulaStream The {@link InputStream} supplying the Modula document to parse.
     * @return The parsed output, the Commons Modula object model corresponding to the Modula document.
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static Modula read(final InputStream modulaStream)
            throws IOException, ModelException, XMLStreamException {

        return read(modulaStream, new Configuration());
    }

    /**
     * Parse the Modula document supplied by the given {@link InputStream} with the given {@link Configuration}.
     *
     * @param modulaStream  The {@link InputStream} supplying the Modula document to parse.
     * @param configuration The {@link Configuration} to use when parsing the Modula document.
     * @return The parsed output, the Commons Modula object model corresponding to the Modula document.
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static Modula read(final InputStream modulaStream, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (modulaStream == null) {
            throw new IllegalArgumentException(ERR_NULL_ISTR);
        }
        Modula modula = readInternal(configuration, null, null, modulaStream, null, null);
        if (modula != null) {
            //ModelUpdater.updateModula(modula);
        }
        return modula;
    }

    //---------------------- PRIVATE UTILITY METHODS ----------------------//

    /**
     * Parse the Modula document at the supplied {@link URL} using the supplied {@link Configuration}, but do not
     * wire up the object model to be usable just yet. Exactly one of the url, path, stream, reader or source
     * parameters must be provided.
     *
     * @param configuration The {@link Configuration} to use when parsing the Modula document.
     * @param modulaURL     The optional Modula document {@link URL} to parse.
     * @param modulaPath    The optional real path to the Modula document as a string.
     * @param modulaStream  The optional {@link InputStream} providing the Modula document.
     * @param modulaReader  The optional {@link Reader} providing the Modula document.
     * @param modulaSource  The optional {@link Source} providing the Modula document.
     * @return The parsed output, the Commons Modula object model corresponding to the Modula document
     * (not wired up to be immediately usable).
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static Modula readInternal(final Configuration configuration, final URL modulaURL, final String modulaPath,
                                       final InputStream modulaStream, final Reader modulaReader, final Source modulaSource)
            throws IOException, ModelException, XMLStreamException {

        if (configuration.pathResolver == null) {
            if (modulaURL != null) {
                configuration.pathResolver = new URLResolver(modulaURL);
            } else if (modulaPath != null) {
                configuration.pathResolver = new URLResolver(new URL(modulaPath));
            }
        }

        XMLStreamReader reader = getReader(configuration, modulaURL, modulaPath, modulaStream, modulaReader, modulaSource);

        return readDocument(reader, configuration);
    }

    /*
     * Private utility functions for reading the Modula document.
     */

    /**
     * Read the Modula document through the {@link XMLStreamReader}.
     *
     * @param reader        The {@link XMLStreamReader} providing the Modula document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @return The parsed output, the Commons Modula object model corresponding to the Modula document
     * (not wired up to be immediately usable).
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static Modula readDocument(final XMLStreamReader reader, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        Modula modula = new Modula();
        while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (ELEM_Modula.equals(name)) {
                        readModula(reader, configuration, modula);
                    } else {
                        reportIgnoredElement(reader, configuration, "DOCUMENT_ROOT", nsURI, name);
                    }
                    break;
                case XMLStreamConstants.NAMESPACE:
                    System.err.println(reader.getNamespaceCount());
                    break;
                default:
            }
        }
        return modula;
    }

    /**
     * Read the contents of this &lt;modula&gt; element.
     *
     * @param reader        The {@link XMLStreamReader} providing the Modula document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param modula        The root of the object model being parsed.
     * @throws IOException        An IO error during parsing.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readModula(final XMLStreamReader reader, final Configuration configuration, final Modula modula)
            throws IOException, ModelException, XMLStreamException {

        if (!MODULA_REQUIRED_VERSION.equals(modula.getVersion())) {
            throw new ModelException(new MessageFormat(ERR_INVALID_VERSION).format(new Object[]{modula.getVersion()}));
        }
        //readNamespaces(configuration, modula);

        boolean hasGlobalScript = false;

//        loop:
//        while (reader.hasNext()) {
//            String name, nsURI;
//            switch (reader.next()) {
//                case XMLStreamConstants.START_ELEMENT:
//                    pushNamespaces(reader, configuration);
//                    nsURI = reader.getNamespaceURI();
//                    name = reader.getLocalName();
//                    if (XMLNS_Modula.equals(nsURI)) {
//                        if (ELEM_STATE.equals(name)) {
//                            readState(reader, configuration, modula, null);
//                        } else if (ELEM_PARALLEL.equals(name)) {
//                            readParallel(reader, configuration, modula, null);
//                        } else if (ELEM_FINAL.equals(name)) {
//                            readFinal(reader, configuration, modula, null);
//                        } else if (ELEM_DATAMODEL.equals(name)) {
//                            readDatamodel(reader, configuration, modula, null);
//                        } else if (ELEM_SCRIPT.equals(name) && !hasGlobalScript) {
//                            readGlobalScript(reader, configuration, modula);
//                            hasGlobalScript = true;
//                        } else {
//                            reportIgnoredElement(reader, configuration, ELEM_Modula, nsURI, name);
//                        }
//                    } else {
//                        reportIgnoredElement(reader, configuration, ELEM_Modula, nsURI, name);
//                    }
//                    break;
//                case XMLStreamConstants.END_ELEMENT:
//                    popNamespaces(reader, configuration);
//                    nsURI = reader.getNamespaceURI();
//                    name = reader.getLocalName();
//                    if (XMLNS_Modula.equals(nsURI) && ELEM_Modula.equals(name)) {
//                        break loop;
//                    }
//                    break;
//                default:
//            }
//        }
    }



    /**
     * Report an ignored element via the {@link XMLReporter} if available and the class
     * {@link org.apache.commons.logging.Log}.
     *
     * @param reader        The {@link XMLStreamReader} providing the Modula document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param parent        The parent element local name in the Modula namespace.
     * @param nsURI         The namespace URI of the ignored element.
     * @param name          The local name of the ignored element.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException     The Commons Modula object model is incomplete or inconsistent (includes
     *                            errors in the Modula document that may not be identified by the schema).
     */
    private static void reportIgnoredElement(final XMLStreamReader reader, final Configuration configuration,
                                             final String parent, final String nsURI, final String name)
            throws XMLStreamException, ModelException {

        org.apache.commons.logging.Log log = LogFactory.getLog(ModulaReader.class);
        StringBuilder sb = new StringBuilder();
        sb.append("Ignoring unknown or invalid element <").append(name)
                .append("> in namespace \"").append(nsURI)
                .append("\" as child of <").append(parent)
                .append("> at ").append(reader.getLocation());
        if (!configuration.isSilent() && log.isWarnEnabled()) {
            log.warn(sb.toString());
        }
        if (configuration.isStrict()) {
            throw new ModelException(sb.toString());
        }
        XMLReporter reporter = configuration.reporter;
        if (reporter != null) {
            reporter.report(sb.toString(), "COMMONS_Modula", null, reader.getLocation());
        }
    }

    /**
     * Push any new namespace declarations on the configuration namespaces map.
     *
     * @param reader        The {@link XMLStreamReader} providing the Modula document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     */
    private static void pushNamespaces(final XMLStreamReader reader, final Configuration configuration) {

        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            Stack<String> stack = configuration.namespaces.get(reader.getNamespacePrefix(i));
            if (stack == null) {
                stack = new Stack<String>();
                configuration.namespaces.put(reader.getNamespacePrefix(i), stack);
            }
            stack.push(reader.getNamespaceURI(i));
        }
    }

    /**
     * Pop any expiring namespace declarations from the configuration namespaces map.
     *
     * @param reader        The {@link XMLStreamReader} providing the Modula document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void popNamespaces(final XMLStreamReader reader, final Configuration configuration)
            throws XMLStreamException {

        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            Stack<String> stack = configuration.namespaces.get(reader.getNamespacePrefix(i));
            if (stack == null) {
                throw new XMLStreamException("Configuration namespaces stack null");
            }
            try {
                stack.pop();
                if (stack.empty()) {
                    configuration.namespaces.remove(reader.getNamespacePrefix(i));
                }
            } catch (EmptyStackException e) {
                throw new XMLStreamException("Configuration namespaces stack popped too many times");
            }
        }
    }

    /**
     * Use the supplied {@link Configuration} to create an appropriate {@link XMLStreamReader} for this
     * {@link ModulaReader}. Exactly one of the url, path, stream, reader or source parameters must be provided.
     *
     * @param configuration The {@link Configuration} to be used.
     * @param url           The {@link URL} to the Modula document to read.
     * @param path          The optional real path to the Modula document as a string.
     * @param stream        The optional {@link InputStream} providing the Modula document.
     * @param reader        The optional {@link Reader} providing the Modula document.
     * @param source        The optional {@link Source} providing the Modula document.
     * @return The appropriately configured {@link XMLStreamReader}.
     * @throws IOException        Exception with the URL IO.
     * @throws XMLStreamException A problem with the XML stream creation or an wrapped {@link SAXException}
     *                            thrown in trying to validate the document against the XML Schema for Modula.
     */
    private static XMLStreamReader getReader(final Configuration configuration, final URL url, final String path,
                                             final InputStream stream, final Reader reader, final Source source)
            throws IOException, XMLStreamException {

        // Instantiate the XMLInputFactory
        XMLInputFactory factory = XMLInputFactory.newInstance();
        if (configuration.factoryId != null && configuration.factoryClassLoader != null) {
            factory = XMLInputFactory.newFactory(configuration.factoryId, configuration.factoryClassLoader);
        }
        factory.setEventAllocator(configuration.allocator);
        for (Map.Entry<String, Object> property : configuration.properties.entrySet()) {
            factory.setProperty(property.getKey(), property.getValue());
        }
        factory.setXMLReporter(configuration.reporter);
        factory.setXMLResolver(configuration.resolver);

        // Consolidate InputStream options
        InputStream urlStream = null;
        if (url != null || path != null) {
            URL modula = (url != null ? url : new URL(path));
            URLConnection conn = modula.openConnection();
            conn.setUseCaches(false);
            urlStream = conn.getInputStream();
        } else if (stream != null) {
            urlStream = stream;
        }

        // Create the XMLStreamReader
        XMLStreamReader xsr = null;

        if (configuration.validate) {
            // Validation requires us to use a Source

            URL modulaSchema = new URL("TODO"); // TODO, point to appropriate location
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = null;
            try {
                schema = schemaFactory.newSchema(modulaSchema);
            } catch (SAXException se) {
                throw new XMLStreamException("Failed to create Modula Schema for validation", se);
            }

            Validator validator = schema.newValidator();
            //validator.setErrorHandler(new SimpleErrorHandler());

            Source src = null;
            if (urlStream != null) {
                // configuration.encoding is ignored
                if (configuration.systemId != null) {
                    src = new StreamSource(urlStream, configuration.systemId);
                } else {
                    src = new StreamSource(urlStream);
                }
            } else if (reader != null) {
                if (configuration.systemId != null) {
                    src = new StreamSource(reader, configuration.systemId);
                } else {
                    src = new StreamSource(reader);
                }
            } else if (source != null) {
                src = source;
            }
            xsr = factory.createXMLStreamReader(src);
            try {
                validator.validate(src);
            } catch (SAXException se) {
                throw new XMLStreamException("Failed to create apply Modula Validator", se);
            }

        } else {
            // We can use the more direct XMLInputFactory API if validation isn't needed

            if (urlStream != null) {
                // systemId gets preference, then encoding if either are present
                if (configuration.systemId != null) {
                    xsr = factory.createXMLStreamReader(configuration.systemId, urlStream);
                } else if (configuration.encoding != null) {
                    xsr = factory.createXMLStreamReader(urlStream, configuration.encoding);
                } else {
                    xsr = factory.createXMLStreamReader(urlStream);
                }
            } else if (reader != null) {
                if (configuration.systemId != null) {
                    xsr = factory.createXMLStreamReader(configuration.systemId, reader);
                } else {
                    xsr = factory.createXMLStreamReader(reader);
                }
            } else if (source != null) {
                xsr = factory.createXMLStreamReader(source);
            }

        }

        return xsr;
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private ModulaReader() {
        super();
    }

    //------------------------- CONFIGURATION CLASS -------------------------//

    /**
     * <p>
     * Configuration for the {@link ModulaReader}. The configuration properties necessary for the following are
     * covered:
     * </p>
     * <p>
     * <ul>
     * <li>{@link XMLInputFactory} configuration properties such as {@link XMLReporter}, {@link XMLResolver} and
     * {@link XMLEventAllocator}</li>
     * <li>{@link XMLStreamReader} configuration properties such as <code>systemId</code> and <code>encoding</code>
     * </li>
     * <li>Commons Modula object model configuration properties such as the list of custom actions and the
     * {@link PathResolver} to use.</li>
     * </ul>
     */
    public static class Configuration {

        /*
         * Configuration properties for this {@link ModulaReader}.
         */
        // XMLInputFactory configuration properties.
        /**
         * The <code>factoryId</code> to use for the {@link XMLInputFactory}.
         */
        final String factoryId;

        /**
         * The {@link ClassLoader} to use for the {@link XMLInputFactory} instance to create.
         */
        final ClassLoader factoryClassLoader;

        /**
         * The {@link XMLEventAllocator} for the {@link XMLInputFactory}.
         */
        final XMLEventAllocator allocator;

        /**
         * The map of properties (keys are property name strings, values are object property values) for the
         * {@link XMLInputFactory}.
         */
        final Map<String, Object> properties;

        /**
         * The {@link XMLResolver} for the {@link XMLInputFactory}.
         */
        final XMLResolver resolver;

        /**
         * The {@link XMLReporter} for the {@link XMLInputFactory}.
         */
        final XMLReporter reporter;

        // XMLStreamReader configuration properties.
        /**
         * The <code>encoding</code> to use for the {@link XMLStreamReader}.
         */
        final String encoding;

        /**
         * The <code>systemId</code> to use for the {@link XMLStreamReader}.
         */
        final String systemId;

        /**
         * Whether to validate the input with the XML Schema for Modula.
         */
        final boolean validate;

        // Commons Modula object model configuration properties.
        /**
         * The list of Commons Modula custom actions that will be available for this document.
         */
        final List<CustomAction> customActions;

        /**
         * The {@link ClassLoader} to use for loading the {@link CustomAction} instances to create.
         */
        final ClassLoader customActionClassLoader;

        /**
         * Whether to use the thread context {@link ClassLoader} for loading any {@link CustomAction} classes.
         */
        final boolean useContextClassLoaderForCustomActions;

        /**
         * The map for bookkeeping the current active namespace declarations. The keys are prefixes and the values are
         * {@link Stack}s containing the corresponding namespaceURIs, with the active one on top.
         */
        final Map<String, Stack<String>> namespaces;

        // Mutable Commons Modula object model configuration properties.
        /**
         * The parent Modula document if this document is src'ed in via the &lt;state&gt; or &lt;parallel&gt; element's
         * "src" attribute.
         */
        Modula parent;

        /**
         * The Commons Modula {@link PathResolver} to use for this document.
         */
        PathResolver pathResolver;

        /**
         * Whether to silently ignore any unknown or invalid elements
         * or to leave warning logs for those.
         */
        boolean silent;

        /**
         * Whether to strictly throw a model exception when there are any unknown or invalid elements
         * or to leniently allow to read the model even with those.
         */
        boolean strict;

        /*
         * Public constructors
         */

        /**
         * Default constructor.
         */
        public Configuration() {
            this(null, null);
        }

        /**
         * Minimal convenience constructor.
         *
         * @param reporter     The {@link XMLReporter} to use for this reading.
         * @param pathResolver The Commons Modula {@link PathResolver} to use for this reading.
         */
        public Configuration(final XMLReporter reporter, final PathResolver pathResolver) {
            this(null, null, null, null, null, reporter, null, null, false, pathResolver, null, null, null, false);
        }

        /**
         * Convenience constructor.
         *
         * @param reporter      The {@link XMLReporter} to use for this reading.
         * @param pathResolver  The Commons Modula {@link PathResolver} to use for this reading.
         * @param customActions The list of Commons Modula custom actions that will be available for this document.
         */
        public Configuration(final XMLReporter reporter, final PathResolver pathResolver,
                             final List<CustomAction> customActions) {
            this(null, null, null, null, null, reporter, null, null, false, pathResolver, null, customActions, null,
                    false);
        }

        /**
         * All purpose constructor. Any of the parameters passed in can be <code>null</code> (booleans should default
         * to <code>false</code>).
         *
         * @param factoryId                             The <code>factoryId</code> to use.
         * @param classLoader                           The {@link ClassLoader} to use for the {@link XMLInputFactory} instance to create.
         * @param allocator                             The {@link XMLEventAllocator} for the {@link XMLInputFactory}.
         * @param properties                            The map of properties (keys are property name strings, values are object property values)
         *                                              for the {@link XMLInputFactory}.
         * @param resolver                              The {@link XMLResolver} for the {@link XMLInputFactory}.
         * @param reporter                              The {@link XMLReporter} for the {@link XMLInputFactory}.
         * @param encoding                              The <code>encoding</code> to use for the {@link XMLStreamReader}
         * @param systemId                              The <code>systemId</code> to use for the {@link XMLStreamReader}
         * @param validate                              Whether to validate the input with the XML Schema for Modula.
         * @param pathResolver                          The Commons Modula {@link PathResolver} to use for this document.
         * @param customActions                         The list of Commons Modula custom actions that will be available for this document.
         * @param customActionClassLoader               The {@link ClassLoader} to use for the {@link CustomAction} instances to
         *                                              create.
         * @param useContextClassLoaderForCustomActions Whether to use the thread context {@link ClassLoader} for the
         *                                              {@link CustomAction} instances to create.
         */
        public Configuration(final String factoryId, final ClassLoader classLoader, final XMLEventAllocator allocator,
                             final Map<String, Object> properties, final XMLResolver resolver, final XMLReporter reporter,
                             final String encoding, final String systemId, final boolean validate, final PathResolver pathResolver,
                             final List<CustomAction> customActions, final ClassLoader customActionClassLoader,
                             final boolean useContextClassLoaderForCustomActions) {
            this(factoryId, classLoader, allocator, properties, resolver, reporter, encoding, systemId, validate,
                    pathResolver, null, customActions, customActionClassLoader,
                    useContextClassLoaderForCustomActions);
        }

        /*
         * Package access constructors
         */

        /**
         * Convenience package access constructor.
         *
         * @param reporter     The {@link XMLReporter} for the {@link XMLInputFactory}.
         * @param pathResolver The Commons Modula {@link PathResolver} to use for this document.
         * @param parent       The parent Modula document if this document is src'ed in via the &lt;state&gt; or
         *                     &lt;parallel&gt; element's "src" attribute.
         */
        Configuration(final XMLReporter reporter, final PathResolver pathResolver, final Modula parent) {
            this(null, null, null, null, null, reporter, null, null, false, pathResolver, parent, null, null, false);
        }

        /**
         * Package access copy constructor.
         *
         * @param source The source {@link Configuration} to replicate.
         */
        Configuration(final Configuration source) {
            this(source.factoryId, source.factoryClassLoader, source.allocator, source.properties, source.resolver,
                    source.reporter, source.encoding, source.systemId, source.validate, source.pathResolver,
                    source.parent, source.customActions, source.customActionClassLoader,
                    source.useContextClassLoaderForCustomActions, source.silent, source.strict);
        }

        /**
         * All-purpose package access constructor.
         *
         * @param factoryId                             The <code>factoryId</code> to use.
         * @param factoryClassLoader                    The {@link ClassLoader} to use for the {@link XMLInputFactory} instance to
         *                                              create.
         * @param allocator                             The {@link XMLEventAllocator} for the {@link XMLInputFactory}.
         * @param properties                            The map of properties (keys are property name strings, values are object property values)
         *                                              for the {@link XMLInputFactory}.
         * @param resolver                              The {@link XMLResolver} for the {@link XMLInputFactory}.
         * @param reporter                              The {@link XMLReporter} for the {@link XMLInputFactory}.
         * @param encoding                              The <code>encoding</code> to use for the {@link XMLStreamReader}
         * @param systemId                              The <code>systemId</code> to use for the {@link XMLStreamReader}
         * @param validate                              Whether to validate the input with the XML Schema for Modula.
         * @param pathResolver                          The Commons Modula {@link PathResolver} to use for this document.
         * @param parent                                The parent Modula document if this document is src'ed in via the &lt;state&gt; or
         *                                              &lt;parallel&gt; element's "src" attribute.
         * @param customActions                         The list of Commons Modula custom actions that will be available for this document.
         * @param customActionClassLoader               The {@link ClassLoader} to use for the {@link CustomAction} instances to
         *                                              create.
         * @param useContextClassLoaderForCustomActions Whether to use the thread context {@link ClassLoader} for the
         *                                              {@link CustomAction} instances to create.
         */
        Configuration(final String factoryId, final ClassLoader factoryClassLoader, final XMLEventAllocator allocator,
                      final Map<String, Object> properties, final XMLResolver resolver, final XMLReporter reporter,
                      final String encoding, final String systemId, final boolean validate, final PathResolver pathResolver,
                      final Modula parent, final List<CustomAction> customActions, final ClassLoader customActionClassLoader,
                      final boolean useContextClassLoaderForCustomActions) {
            this(factoryId, factoryClassLoader, allocator, properties, resolver, reporter, encoding, systemId,
                    validate, pathResolver, parent, customActions, customActionClassLoader,
                    useContextClassLoaderForCustomActions, false, false);
        }

        /**
         * All-purpose package access constructor.
         *
         * @param factoryId                             The <code>factoryId</code> to use.
         * @param factoryClassLoader                    The {@link ClassLoader} to use for the {@link XMLInputFactory} instance to
         *                                              create.
         * @param allocator                             The {@link XMLEventAllocator} for the {@link XMLInputFactory}.
         * @param properties                            The map of properties (keys are property name strings, values are object property values)
         *                                              for the {@link XMLInputFactory}.
         * @param resolver                              The {@link XMLResolver} for the {@link XMLInputFactory}.
         * @param reporter                              The {@link XMLReporter} for the {@link XMLInputFactory}.
         * @param encoding                              The <code>encoding</code> to use for the {@link XMLStreamReader}
         * @param systemId                              The <code>systemId</code> to use for the {@link XMLStreamReader}
         * @param validate                              Whether to validate the input with the XML Schema for Modula.
         * @param pathResolver                          The Commons Modula {@link PathResolver} to use for this document.
         * @param parent                                The parent Modula document if this document is src'ed in via the &lt;state&gt; or
         *                                              &lt;parallel&gt; element's "src" attribute.
         * @param customActions                         The list of Commons Modula custom actions that will be available for this document.
         * @param customActionClassLoader               The {@link ClassLoader} to use for the {@link CustomAction} instances to
         *                                              create.
         * @param useContextClassLoaderForCustomActions Whether to use the thread context {@link ClassLoader} for the
         *                                              {@link CustomAction} instances to create.
         * @param silent                                Whether to silently ignore any unknown or invalid elements or to leave warning logs for those.
         * @param strict                                Whether to strictly throw a model exception when there are any unknown or invalid elements
         *                                              or to leniently allow to read the model even with those.
         */
        Configuration(final String factoryId, final ClassLoader factoryClassLoader, final XMLEventAllocator allocator,
                      final Map<String, Object> properties, final XMLResolver resolver, final XMLReporter reporter,
                      final String encoding, final String systemId, final boolean validate, final PathResolver pathResolver,
                      final Modula parent, final List<CustomAction> customActions, final ClassLoader customActionClassLoader,
                      final boolean useContextClassLoaderForCustomActions, final boolean silent, final boolean strict) {
            this.factoryId = factoryId;
            this.factoryClassLoader = factoryClassLoader;
            this.allocator = allocator;
            this.properties = (properties == null ? new HashMap<String, Object>() : properties);
            this.resolver = resolver;
            this.reporter = reporter;
            this.encoding = encoding;
            this.systemId = systemId;
            this.validate = validate;
            this.pathResolver = pathResolver;
            this.parent = parent;
            this.customActions = (customActions == null ? new ArrayList<CustomAction>() : customActions);
            this.customActionClassLoader = customActionClassLoader;
            this.useContextClassLoaderForCustomActions = useContextClassLoaderForCustomActions;
            this.namespaces = new HashMap<String, Stack<String>>();
            this.silent = silent;
            this.strict = strict;
        }

        /*
         * Package access convenience methods
         */

        /**
         * Get the current namespaces at this point in the StAX reading.
         *
         * @return Map<String,String> The namespace map (keys are prefixes and values are the corresponding current
         * namespace URIs).
         */
        Map<String, String> getCurrentNamespaces() {
            Map<String, String> currentNamespaces = new HashMap<String, String>();
            for (Map.Entry<String, Stack<String>> nsEntry : namespaces.entrySet()) {
                currentNamespaces.put(nsEntry.getKey(), nsEntry.getValue().peek());
            }
            return currentNamespaces;
        }

        /**
         * Returns true if it is set to read models silently without any model error warning logs.
         *
         * @return
         * @see {@link #silent}
         */
        public boolean isSilent() {
            return silent;
        }

        /**
         * Turn on/off silent mode (whether to read models silently without any model error warning logs)
         *
         * @param silent
         * @see {@link #silent}
         */
        public void setSilent(boolean silent) {
            this.silent = silent;
        }

        /**
         * Returns true if it is set to check model strictly with throwing exceptions on any model error.
         *
         * @return
         * @see {@link #strict}
         */
        public boolean isStrict() {
            return strict;
        }

        /**
         * Turn on/off strict model (whether to check model strictly with throwing exception on any model error)
         *
         * @param strict
         * @see {@link #strict}
         */
        public void setStrict(boolean strict) {
            this.strict = strict;
        }
    }
}


