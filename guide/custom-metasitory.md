<div class="page-header">
    <h2>Custom Metasitory</h2>
</div>

You can create your own `Metasitory` implementation if you need to. In this guide, we'll create one based on an `XML` file. I don't think it's a good idea but it's good enough for the illustration.

So, the idea here's that we want to generate an `XML` file which holds all the required information about metacode: masters, their metacode and the annotations they use.

First, we need to create a `MetasitoryWriter`. This class generates a metacode store, i.e.`XML` file. As in case of [custom processor](/guide/custom-processor.html) it must be a separate module. For this example we won't use any modern tools to built `XML` and will be writing it as plain text:

    :::java
    public class XmlMetasitoryWriter implements MetasitoryWriter {
        private Writer xml;

        @Override
        public void open(ProcessingContext processingContext) {
            try {
                FileObject fileObject = processingContext.processingEnv().getFiler().createResource(
                        StandardLocation.SOURCE_OUTPUT,
                        "",
                        "metasitory.xml");
                xml = fileObject.openWriter();
                xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                xml.append("<items>\n");

            } catch (IOException e) {
                throw new ProcessingException(e);
            }
        }

        @Override
        public void write(MetacodeContext metacodeContext) {
            try {
                String master = metacodeContext.masterElement().getQualifiedName().toString();
                xml.append("\t<item master=\"")
                        .append(master)
                        .append("\" metacode=\"")
                        .append(MetacodeUtils.toMetacodeName(master))
                        .append("\">\n");

                for (Class<? extends Annotation> annotation : metacodeContext.metacodeAnnotations()) {
                    xml.append("\t\t<annotation>")
                            .append(annotation.getCanonicalName())
                            .append("</annotation>\n");
                }

                xml.append("\t</item>\n");

            } catch (IOException e) {
                throw new ProcessingException(e);
            }
        }

        @Override
        public void close() {
            try {
                xml.append("</items>");
                xml.close();

            } catch (IOException e) {
                throw new ProcessingException(e);
            }
        }
    }

To say to *Jeta* to use this metasitory writer, add this property into `jeta.properties`:

    :::properties
    metasitory.writer=com.example.metasitory.XmlMetasitoryWriter


After you assemble your project, we'll see `metasitory.xml` file in `build/generated/source/apt/main/` folder. Here's the its listing for this sample:

    :::xml
    <?xml version="1.0" encoding="UTF-8"?>
    <items>
        <item master="com.example.HelloWorldSample" metacode="com.example.HelloWorldSample_Metacode">
            <annotation>com.example.SayHello</annotation>
        </item>
    </items>


We won't create actual `Metasitory` implementation for this file. There's no difficulties to do that though. It parses `metasitory.xml` and use its structure to search metacode by given `Criteria`. Here's the listing of `Metasitory` interface it must implement in order to support *Jeta* API:

    :::java
    public interface Metasitory {
        Collection<IMetacode<?>> search(Criteria criteria);
        void add(Metasitory other);
    }

You can refer to [MapMetasitory](https://github.com/brooth/jeta/blob/master/jeta/src/main/java/org/brooth/jeta/metasitory/MapMetasitory.java) implementation as the example.

This sample is available on [GitHub](https://github.com/brooth/jeta-samples).
