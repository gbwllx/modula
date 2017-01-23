package modula.parser.io;

import modula.parser.env.URLResolver;
import modula.parser.model.*;
import modula.parser.PathResolver;
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

    /**
     * Modula命名空间
     */
    private static final String XMLNS_MODULA =
            "http://localhost/2017/01/modula";

    /**
     * 默认命名空间
     */
    private static final String XMLNS_DEFAULT = null;

    //---- ERROR MESSAGES ----//
    /**
     * 空URL
     */
    private static final String ERR_NULL_URL = "Cannot parse null URL";

    /**
     * 空PATH
     */
    private static final String ERR_NULL_PATH = "Cannot parse null path";

//    /**
//     * 空InputStream
//     */
//    private static final String ERR_NULL_ISTR = "Cannot parse null InputStream";
//
//    /**
//     * 空Reader
//     */
//    private static final String ERR_NULL_READ = "Cannot parse null Reader";
//
//    /**
//     * 空Source
//     */
//    private static final String ERR_NULL_SRC = "Cannot parse null Source";
//
//    /**
//     * Action定义错误
//     */
//    private static final String ERR_CUSTOM_ACTION_TYPE = "Custom actions list"
//            + " contained unknown object, class not a Commons Modula Action class subtype: ";
//
//    /**
//     * 解析DOM树错误
//     */
//    private static final String ERR_PARSER_CFG = "ParserConfigurationException while trying"
//            + " to parse stream into DOM node(s).";
//
//    /**
//     * 找不到定义文档
//     */
//    private static final String ERR_STATE_SRC =
//            "Source attribute in <state src=\"{0}\"> cannot be parsed";
//
//    /**
//     * 未知的state
//     */
//    private static final String ERR_STATE_SRC_FRAGMENT = "URI Fragment in "
//            + "<state src=\"{0}\"> is an unknown state in referenced document";
//
//    /**
//     * state的src指向非state或final
//     */
//    private static final String ERR_STATE_SRC_FRAGMENT_TARGET = "URI Fragment"
//            + " in <state src=\"{0}\"> does not point to a <state> or <final>";
//
    /**
     * 必填属性缺失
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
    private static final String ELEM_FINAL = "final";
    private static final String ELEM_INITIAL = "initial";
    private static final String ELEM_LOG = "log";
    private static final String ELEM_ONENTRY = "onentry";
    private static final String ELEM_ONEXIT = "onexit";
    private static final String ELEM_MODULA = "modula";
    private static final String ELEM_STATE = "state";
    private static final String ELEM_TRANSITION = "transition";

    //---- ATTRIBUTE NAMES ----//
    private static final String ATTR_COND = "cond";
    private static final String ATTR_EVENT = "event";
    private static final String ATTR_EXPR = "expr";
    private static final String ATTR_ID = "id";
    private static final String ATTR_INITIAL = "initial";
    private static final String ATTR_LABEL = "label";
    //;private static final String ATTR_SRC = "src";
    private static final String ATTR_TARGET = "target";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_VERSION = "version";

    //------------------------- PUBLIC API METHODS -------------------------//
    /*
     * Public methods
     */

    /**
     * 使用{@link Configuration}解析Modula文档，{@link URL}
     *
     * @param modulaURL     {@link URL}
     * @param configuration {@link Configuration}
     * @return Modula
     */
    public static Modula read(final URL modulaURL, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (modulaURL == null) {
            throw new IllegalArgumentException(ERR_NULL_URL);
        }
        Modula modula = readInternal(configuration, modulaURL, null, null, null, null);
        if (modula != null) {
            ModelUpdater.updateModula(modula);
        }
        return modula;
    }

    //---------------------- PRIVATE UTILITY METHODS ----------------------//

    /**
     * 解析Modula文档
     *
     * @return Modula
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

    /**
     * 通过{@link XMLStreamReader}解析文档
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
                    if (ELEM_MODULA.equals(name)) {
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
     * 解析modula内容
     */
    private static void readModula(final XMLStreamReader reader, final Configuration configuration, final Modula modula)
            throws IOException, ModelException, XMLStreamException {
        modula.setInitial(readAV(reader, ATTR_INITIAL));
        modula.setVersion(readRequiredAV(reader, ELEM_MODULA, ATTR_VERSION));
        if (!MODULA_REQUIRED_VERSION.equals(modula.getVersion())) {
            throw new ModelException(new MessageFormat(ERR_INVALID_VERSION).format(new Object[]{modula.getVersion()}));
        }
        readNamespaces(configuration, modula);

        loop:
        while (reader.hasNext()) {
            String name, nsURI;
            int next = reader.next();
            switch (next) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI)) {
                        if (ELEM_STATE.equals(name)) {
                            readState(reader, configuration, modula, null);
                        } else if (ELEM_FINAL.equals(name)) {
                            readFinal(reader, configuration, modula, null);
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_MODULA, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_MODULA, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI) && ELEM_MODULA.equals(name)) {
                        break loop;
                    }
                    break;
                default:
            }
        }
    }

    /**
     * 解析state内容
     */
    private static void readState(final XMLStreamReader reader, final Configuration configuration, final Modula modula,
                                  final TransitionalState parent)
            throws IOException, ModelException, XMLStreamException {

        State state = new State();
        state.setId(readOrGeneratedTransitionTargetId(reader, modula, ELEM_STATE));
        String initial = readAV(reader, ATTR_INITIAL);
        if (initial != null) {
            state.setFirst(initial);
        }

        if (parent == null) {
            modula.addChild(state);
        } else if (parent instanceof State) {//混合状态，保留
            ((State) parent).addChild(state);
        }
        modula.addTarget(state);
        if (configuration.parent != null) {
            configuration.parent.addTarget(state);
        }

        loop:
        while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI)) {
                        if (ELEM_TRANSITION.equals(name)) {
                            state.addTransition(readTransition(reader, configuration));
                        } else if (ELEM_STATE.equals(name)) {//混合state，目前没有这种情况，但先保留
                            readState(reader, configuration, modula, state);
                        } else if (ELEM_INITIAL.equals(name)) {
                            readInitial(reader, configuration, state);
                        } else if (ELEM_FINAL.equals(name)) {
                            readFinal(reader, configuration, modula, state);
                        } else if (ELEM_ONENTRY.equals(name)) {
                            readOnEntry(reader, configuration, state);
                        } else if (ELEM_ONEXIT.equals(name)) {
                            readOnExit(reader, configuration, state);
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_STATE, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_STATE, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI) && ELEM_STATE.equals(name)) {
                        break loop;
                    }
                    break;
                default:
            }
        }
    }

    private static Transition readTransition(final XMLStreamReader reader, final Configuration configuration)
            throws XMLStreamException, ModelException {

        Transition transition = new Transition();
        transition.setCond(readAV(reader, ATTR_COND));
        transition.setEvent(readAV(reader, ATTR_EVENT));
        transition.setNext(readAV(reader, ATTR_TARGET));
        String type = readAV(reader, ATTR_TYPE);
        if (type != null) {
            try {
                transition.setType(TransitionType.valueOf(type));
            } catch (IllegalArgumentException e) {
                MessageFormat msgFormat = new MessageFormat(ERR_UNSUPPORTED_TRANSITION_TYPE);
                String errMsg = msgFormat.format(new Object[]{type, reader.getLocation()});
                throw new ModelException(errMsg);
            }
        }

        readNamespaces(configuration, transition);
        readExecutableContext(reader, configuration, transition);

        return transition;
    }

    private static void readInitial(final XMLStreamReader reader, final Configuration configuration,
                                    final State state)
            throws XMLStreamException, ModelException {

        Initial initial = new Initial();

        loop:
        while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI)) {
                        if (ELEM_TRANSITION.equals(name)) {
                            initial.setTransition(readSimpleTransition(reader, configuration));
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_INITIAL, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_INITIAL, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI) && ELEM_INITIAL.equals(name)) {
                        break loop;
                    }
                    break;
                default:
            }
        }

        state.setInitial(initial);
    }

    private static SimpleTransition readSimpleTransition(final XMLStreamReader reader, final Configuration configuration)
            throws XMLStreamException, ModelException {

        SimpleTransition transition = new SimpleTransition();
        transition.setNext(readAV(reader, ATTR_TARGET));
        String type = readAV(reader, ATTR_TYPE);
        if (type != null) {
            try {
                transition.setType(TransitionType.valueOf(type));
            } catch (IllegalArgumentException e) {
                MessageFormat msgFormat = new MessageFormat(ERR_UNSUPPORTED_TRANSITION_TYPE);
                String errMsg = msgFormat.format(new Object[]{type, reader.getLocation()});
                throw new ModelException(errMsg);
            }
        }

        readNamespaces(configuration, transition);
        readExecutableContext(reader, configuration, transition);

        return transition;
    }


    /**
     * 解析Final
     */
    private static void readFinal(final XMLStreamReader reader, final Configuration configuration, final Modula modula,
                                  final State parent)
            throws XMLStreamException, ModelException, IOException {

        Final end = new Final();
        end.setId(readOrGeneratedTransitionTargetId(reader, modula, ELEM_FINAL));

        if (parent == null) {
            modula.addChild(end);
        } else {
            parent.addChild(end);
        }

        modula.addTarget(end);
        if (configuration.parent != null) {
            configuration.parent.addTarget(end);
        }

        loop:
        while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI)) {
                        if (ELEM_ONENTRY.equals(name)) {
                            readOnEntry(reader, configuration, end);
                        } else if (ELEM_ONEXIT.equals(name)) {
                            readOnExit(reader, configuration, end);
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_FINAL, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_FINAL, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI) && ELEM_FINAL.equals(name)) {
                        break loop;
                    }
                    break;
                default:
            }
        }
    }

    private static void readOnExit(final XMLStreamReader reader, final Configuration configuration,
                                   final EnterableState es)
            throws XMLStreamException, ModelException {

        OnExit onexit = new OnExit();
        onexit.setRaiseEvent(readBooleanAV(reader, ELEM_ONEXIT, ATTR_EVENT));
        readExecutableContext(reader, configuration, onexit);
        es.addOnExit(onexit);
    }

    private static void readOnEntry(final XMLStreamReader reader, final Configuration configuration,
                                    final EnterableState es)
            throws XMLStreamException, ModelException {

        OnEntry onentry = new OnEntry();
        onentry.setRaiseEvent(readBooleanAV(reader, ELEM_ONENTRY, ATTR_EVENT));
        readExecutableContext(reader, configuration, onentry);
        es.addOnEntry(onentry);
    }

    private static void readExecutableContext(final XMLStreamReader reader, final Configuration configuration,
                                              final Executable executable)
            throws XMLStreamException, ModelException {

        String end = "";

        if (executable instanceof SimpleTransition) {
            end = ELEM_TRANSITION;
        } else if (executable instanceof OnEntry) {
            end = ELEM_ONENTRY;
        } else if (executable instanceof OnExit) {
            end = ELEM_ONEXIT;
        }

        loop:
        while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI)) {
                        if (ELEM_LOG.equals(name)) {
                            readLog(reader, configuration, executable);
                        } else {
                            reportIgnoredElement(reader, configuration, end, nsURI, name);
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_MODULA.equals(nsURI) && end.equals(name)) {
                        break loop;
                    }
                    break;
                default:
            }
        }
    }

    private static void readLog(final XMLStreamReader reader, final Configuration configuration,
                                final Executable executable)
            throws XMLStreamException {

        Log log = new Log();
        log.setExpr(readAV(reader, ATTR_EXPR));
        log.setLabel(readAV(reader, ATTR_LABEL));
        readNamespaces(configuration, log);
        log.setParent(executable);

        executable.addAction(log);

    }

    private static String nullIfEmpty(String input) {
        return input == null || input.trim().length() == 0 ? null : input.trim();
    }

    /**
     * 读取属性值
     */
    private static String readAV(final XMLStreamReader reader, final String attrLocalName) {
        return nullIfEmpty(reader.getAttributeValue(XMLNS_DEFAULT, attrLocalName));
    }

    private static Boolean readBooleanAV(final XMLStreamReader reader, final String elementName,
                                         final String attrLocalName)
            throws ModelException {
        String value = nullIfEmpty(reader.getAttributeValue(XMLNS_DEFAULT, attrLocalName));
        Boolean result = "true".equals(value) ? Boolean.TRUE : "false".equals(value) ? Boolean.FALSE : null;
        if (result == null && value != null) {
            MessageFormat msgFormat = new MessageFormat(ERR_ATTRIBUTE_NOT_BOOLEAN);
            String errMsg = msgFormat.format(new Object[]{value, attrLocalName, elementName, reader.getLocation()});
            throw new ModelException(errMsg);
        }
        return result;
    }

    /**
     * 读取必填属性值
     */
    private static String readRequiredAV(final XMLStreamReader reader, final String elementName, final String attrLocalName)
            throws ModelException {
        String value = nullIfEmpty(reader.getAttributeValue(XMLNS_DEFAULT, attrLocalName));
        if (value == null) {
            MessageFormat msgFormat = new MessageFormat(ERR_REQUIRED_ATTRIBUTE_MISSING);
            String errMsg = msgFormat.format(new Object[]{elementName, attrLocalName, reader.getLocation()});
            throw new ModelException(errMsg);
        }
        return value;
    }

    private static String readOrGeneratedTransitionTargetId(final XMLStreamReader reader, final Modula modula,
                                                            final String elementName)
            throws ModelException {
        String id = readAV(reader, ATTR_ID);
        if (id == null) {
            id = modula.generateTransitionTargetId();
        } else if (id.startsWith(Modula.GENERATED_TT_ID_PREFIX)) {
            MessageFormat msgFormat = new MessageFormat(ERR_RESERVED_ID_PREFIX);
            String errMsg = msgFormat.format(new Object[]{elementName, id, reader.getLocation()});
            throw new ModelException(errMsg);
        }
        return id;
    }

    private static void readNamespaces(final Configuration configuration, final NamespacePrefixesHolder holder) {
        holder.setNamespaces(configuration.getCurrentNamespaces());
    }

    /**
     * 通过{@link XMLReporter}上报未知元素
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
     * 命名空间 入栈
     *
     * @param reader
     * @param configuration
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
     * 创建{@link XMLStreamReader} 实例
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
     * {@link ModulaReader}配置信息，以下信息必须提供：
     * {@link XMLInputFactory}：{@link XMLReporter}, {@link XMLResolver}，{@link XMLEventAllocator}
     * {@link XMLStreamReader}：systemId，encoding
     * CustomActions
     * {@link PathResolver}
     */
    public static class Configuration {
        // XMLInputFactory相关属性
        /**
         * factoryId
         */
        final String factoryId;

        /**
         * xmlInputFactory加载ClassLoader
         */
        final ClassLoader factoryClassLoader;

        /**
         * key:属性名
         */
        final Map<String, Object> properties;

        /**
         * XMLResolver
         */
        final XMLResolver resolver;

        /**
         * XMLReporter
         */
        final XMLReporter reporter;

        /**
         * XMLEventAllocator
         */
        final XMLEventAllocator allocator;

        // XMLStreamReader相关属性
        /**
         * encoding
         */
        final String encoding;

        /**
         * systemId
         */
        final String systemId;

        /**
         * 是否校验文档
         */
        final boolean validate;

        // Modula相关配置
        /**
         * CustomAction列表
         */
        final List<CustomAction> customActions;

        /**
         * customAction加载ClassLoader
         */
        final ClassLoader customActionClassLoader;

        /**
         * 是否使用线程上下文加载customAction
         */
        final boolean useContextClassLoaderForCustomActions;

        /**
         * 命名空间，value保存在{@link Stack}s，当前活动的在最上面
         */
        final Map<String, Stack<String>> namespaces;

        // Modula可变配置
        /**
         * 父文档，在state src配置时
         */
        Modula parent;

        /**
         * PathResolver
         */
        PathResolver pathResolver;

        /**
         * 未知或非法元素处理，打日志或忽略
         */
        boolean silent;

        /**
         * 未知或非法元素处理，是否抛出异常
         */
        boolean strict;

        /*
         * Public 构造器
         */

        /**
         * 最小构造器
         */
        public Configuration(final XMLReporter reporter, final PathResolver pathResolver) {
            this(null, null, null, null, null, reporter, null, null, false, pathResolver, null, null, null, false);
        }

        /**
         * 支持customActions构造器
         */
        public Configuration(final XMLReporter reporter, final PathResolver pathResolver,
                             final List<CustomAction> customActions) {
            this(null, null, null, null, null, reporter, null, null, false, pathResolver, null, customActions, null,
                    false);
        }

        /**
         * 复制构造器
         */
        Configuration(final Configuration source) {
            this(source.factoryId, source.factoryClassLoader, source.allocator, source.properties, source.resolver,
                    source.reporter, source.encoding, source.systemId, source.validate, source.pathResolver,
                    source.parent, source.customActions, source.customActionClassLoader,
                    source.useContextClassLoaderForCustomActions, source.silent, source.strict);
        }

        /**
         * 全属性构造器
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
         * 全属性构造器，支持silent和strict配置
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
         * 获取命名空间
         */
        Map<String, String> getCurrentNamespaces() {
            Map<String, String> currentNamespaces = new HashMap<String, String>();
            for (Map.Entry<String, Stack<String>> nsEntry : namespaces.entrySet()) {
                currentNamespaces.put(nsEntry.getKey(), nsEntry.getValue().peek());
            }
            return currentNamespaces;
        }

        /**
         * 是否打错误日志
         */
        public boolean isSilent() {
            return silent;
        }

        public void setSilent(boolean silent) {
            this.silent = silent;
        }

        /**
         * 是否抛出异常
         */
        public boolean isStrict() {
            return strict;
        }

        public void setStrict(boolean strict) {
            this.strict = strict;
        }
    }
}


