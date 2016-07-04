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