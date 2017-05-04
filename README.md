# Commons
Ad-hoc collection of re-usable Java classes.

[![](https://jitpack.io/v/hindol/commons.svg)](https://jitpack.io/#hindol/commons)

## Argument

Reads configuration parameters from environment variables and Java system properties.

### Usage

```java
String javaHome = Argument.resolver()
        .firstTry("javaHome", Argument.Type.PROPERTY) // -DjavaHome="..."
        .thenTry("JAVA_HOME", Argument.Type.ENVIRONMENT_VARIABLE) // export JAVA_HOME=...
        .orElse("/usr/lib/jvm/openjdk-8-jdk/")
        .resolve();
```

## BackOffPolicy

Back-off policy for anything web.

### Usage

```java
BackOffPolicy backOffPolicy = new ExponentialBackOffPolicy.Builder()
		.setInitialInterval(1, TimeUnit.SECONDS)
        .setMaxInterval(1, TimeUnit.MINUTES)
        .setMultiplier(1.5)
        .setMaxElapsedTime(15, TimeUnit.MINUTES)
        .setRandomizationFactor(0.5)
        .build();

// Pseudo code, won't compile
while (true) {
	try {
    	String response = url.fetch();
        backOffPolicy.reset();
    } catch (IOException ioe) {
    	long interval = backOffPolicy.nextIntervalMillis();
        if (interval != BackOffPolicy.STOP) {
        	Thread.sleep(interval);
        } else {
        	throw e;
        }
    }
}
```

## DirectoryWatcher

Watches one or more directories for changes using Java 7's WatchService API.

### Usage

```java
DirectoryWatcher watcher = new DirectoryWatcher.Builder()
        .addDirectories("/home/hindol/")
        .setPreExistingAsCreated(true)
        .build(new DirectoryWatcher.Listener() {
            public void onEvent(DirectoryWatcher.Event event, Path path) {
                switch (event) {
                    case ENTRY_CREATE:
                        System.out.println(path + " created.");
                        break;

                    case ENTRY_MODIFY:
                        ...

                    case ENTRY_DELETE:
                        ...
                }
            }
        });

try {
    watcher.start(); // Actual watching starts here
    TimeUnit.SECONDS.sleep(30);
    watcher.stop(); // Stop watching
} catch (Exception e) {
    // Do something
}
```

## XML

DSL like XML creation.

### Usage

```java
String xml = Xml.createWriter()
                    .beginElement("VAST")
                        .attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                        .attribute("xsi:noNamespaceSchemaLocation", "vast.xsd")
                        .attribute("version", "3.0")
                        .beginElement("Ad")
                            .attribute("id", "xyz")
                            .beginElement("Wrapper")
                                .element("AdSystem", "Vizury")
                                .element("VASTAdTagURI", Xml.wrapInCdata("https://www.vizury.com/"))
                                .element("Error", Xml.wrapInCdata("https://www.vizury.com/"))
                                .element("Impression", Xml.wrapInCdata("https://www.vizury.com/"))
                                .beginElement("Creatives")
                                    .beginElement("Creative")
                                        .attribute("AdID", "xyz")
                                    .endElement()
                                .endElement()
                            .endElement()
                        .endElement()
                    .endElement().toXml();
```

The above code creates the following XML (minus the indentation of course),

```xml
<VAST xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:noNamespaceSchemaLocation="vast.xsd"
      version="3.0">
    <Ad id="xyz">
        <Wrapper>
            <AdSystem>Vizury</AdSystem>
            <VASTAdTagURI><![CDATA[https://www.vizury.com/]]></VASTAdTagURI>
            <Error><![CDATA[https://www.vizury.com/]]></Error>
            <Impression><![CDATA[https://www.vizury.com/]]></Impression>
            <Creatives><Creative AdID="xyz" /></Creatives>
        </Wrapper>
    </Ad>
</VAST>
```

## Template

Simple string templates.

### Usage

```java
// Use '{{' and '}}' as marker (default).
Template template = Template.compile("To {{verb}} or not to {{verb}}.");
String sentence = template.format("verb", "be");
    
// Use '__' instead.
Template template = Template.createEngine("__", "__").compile("I tried to __verb__ your __noun__.");
String sentence = template.format("verb", "roast", "noun", "toast");
```