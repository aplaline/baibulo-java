# Baibulo - Java implementation

Baibulo (version in Chewa) is a versioned static content server and manager package for Node express applications. It is a version of implementation of approach presented on RailsConf 2014 by Luke Melia.

## Usage

The solution comes in the form of a servlet `com.aplaline.baibulo.StaticContentManager`. The following is a basic usage example:

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
        version="3.0">

        <servlet>
                <servlet-name>Baibulo</servlet-name>
                <servlet-class>com.aplaline.baibulo.StaticContentManager</servlet-class>
                <init-param>
                        <param-name>root</param-name>
                        <param-value>/tmp/baibulo</param-value>
                </init-param>
                <init-param>
                        <param-name>upload-enabled</param-name>
                        <param-value>true</param-value>
                </init-param>
        </servlet>
        <servlet-mapping>
                <servlet-name>Baibulo</servlet-name>
                <url-pattern>/assets/*</url-pattern>
        </servlet-mapping>

</web-app>
```

As you can see there are 2 different parameters that can be adjusted:

`root` - the root folder on the filesystem that all versioned files will be stored in

`upload-enabled` - a flag that enables uploading of new content. Setting it to `false` (for example in production) disables the upload thus providing a secure way of serving content to the general public. Nothing stands in the way of having additional server running on an internal IP address that would allow for upload. After all this is just file system that is being used as storage.

The `servlet-mapping` determines what will be the root URL for all content. So for example if the context name is `hello`, the `servlet-mapping`'s `url-pattern` points to `/assets/*` and the file name is `image.png` then the full path will be `/hello/assets/image.png`.

## Deployment

The deployment can be done either using cURL or with a dedicated utility called `baibulo-deploy` written as a Node.js package. See https://github.com/aplaline/baibulo-deploy for further information about that utility.

For now let's concentrate on how to deploy a single file in a specific version using cURL.

```
curl -v -X PUT \
  --data-binary "@image.png" \
  -H "Version: TST-1234" \
  http://localhost:8080/hello/assets/image.png
```

Alternatively to the `Version` header you can use the query string parameter named `version` like so:

```
curl -v -X PUT \
  --data-binary "@image.png" \
  http://localhost:8080/hello/assets/image.png?version=TST-1234
```

## Retrieval rules

When retrieving content Baibulo has 4 stages at which it tries to determine the version which should be served:

1. Query string parameter named `version`
2. Header `Version`
3. Header `Referrer` and its query string parameter `version`
4. Cookie `__version`

If none will be found then the version name `release` will be used.

## Storage options

Baibulo stores the content of static assets in folders with the name of the file and underneeth it there are files with the actual version name. For a simple `index.html` in version TST-1234 (mimicing a Jira ticket number) the structure would look like that:

```
/
 /index.html
   /TST-1234
```

In the future there will be options to store the assets in other storages, such as SQL and NoSQL databases, maybe even in S3 or other cloud storages.

## Development

The project is managed using Maven. Simply import it to your favorite IDE and get hacking!

## Deployment to central

To deploy to central you need to have the proper Maven setup. You can read more about it on https://central.sonatype.org/pages/apache-maven.html.

Command for running the release:

```
$ mvn release:prepare -P release
$ mvn release:perform -P release
```

The release profile enables GPG signing required by Sonatype's OSS repository and sets the version numbering to be `@{project.version}` instead of the default combination of `@{project.artifactId}-@{project.version}` - just because it is cleaner.
