# Building the Judoscript language and its Website

The Judoscript software and its website had been built in a coherent manner. The only reason for this is to share the language documentation.

The website, judoscript.com, had undergone one major change, hence there was a [version 1 site](judoscript.com-v1) and a [version 2 site](judoscript.com-v2). Both these sites are, appropriately, reconstructed and available in this museum.

The greater significance, however, lies in the fact that _everything_, from the langauge software to documentation to website, is built with Judoscript programs. This is a great way to showcases the capability and stability of the language, even though, inevitably, the used features are less business-related.

When the language became stable enough, new ideas kept popping up, because, truly, the sky was the limit. One of the urges that I could not resist was _JuSP_; JuSP to Judoscript is like JSP to Java. Given the then adoption status of Judoscript, even I would not expect a stellar welcome of JuSP; yet, with a slight expansion, _JuSPT_ (JuSP Transform) was available, and became a wonderful document building tool. This has a great impact on how the new website was built; yes, it is all JuSPT based!

## The evolution of Judoscript building process

The building processes during site version 1 and version 2 are quite different.

At the beginning, all documentations are within the source tree itself. The documentation modules included _articles_, _references_ and _examples_. The examples are also known as _test cases_, which serve the role of the best way of documentation, in fact.

Soon, due to the grandeur of the idea, mere documentation was clearly not enough, and a comprehensive book was needed. Thus, a book writing effort was initiated. Little by little, more articles were shifted into that book project, and those web links became redirects to the book chapters.

Then the look-and-feel of the website needed to improved, so it was redesigned. And JuSP/JuSPT was just developped; after some struggle (of decision making), JuSPT was adopted to build the new site. And it turned out to be a great success. In doing so, the (remaining) articles and reference sources were all converted into JuSP style, and the build process was more segregated from the software building process. Yet, the examples (also known as test cases) remained in the Judoscritp source code tree.

## The build processes

The building process for each release is an initertwined operation of both the software and website content, like this:

1. Build the software executables, source code bundles, etc. (in a temporary location).
2. Build all the documentation, including examples, into a folder, ready for the website.
3. Package the Judoscript zip file with all the executables, artifacts, and the documentation; put everything into a location that is ready for the website.
4. Finally, rebuild a few web pages such as <code>index.html</code>, <code>download.html</code>, release notes, etc.

Under the <code>projects/</code> folder, there are various modules; most of them have a <code>buildtools/</code> folder that contains the module-specific building scripts, all written in Judoscript. Therefore, you will need a _Judoscript executable_ to build Judoscript; that is available in the <code>bin/</code> folder at the top. Most of the build tools are menu-driven, so they are relatively easy to use.

The building process requires two environment variables to be set: <code>JAVA_HOME</code> and <code>JUDOROOT</code>. A third environment variable, <code>SITE_VERSION</code>, is optional, and the default is <code>v2</code>. To build Judoscript executable for a specific version of Java, set <code>JAVA_HOME</code> to the appropriate JDK version. The parser generator is **javacc**, and a particular version of which is included in the <code>bin/</code> folder.

The library files, copied from various sources; and, please, no intention to violate any copyrights or copylefts here, in case some ignorrence does occur. As later Java versions are becoming more and more all-encompassing, the hope is to rely on the Java runtime without any of those extra libraries, or simply implement what is needed into the Judoscript code base itself.

The steps of building are different for version 1 and version 2. I will try to document as accurately as possible here.

Before either build, first, set up the environment:

1. Set up environment variables <code>JAVA_HOME</code> and <code>JUDOROOT</code> (see <code>projects/common.judi</code> for how these are used).
2. Create an alias like: <code>judo="$JAVA_HOME/bin/java -jar $JUDOROOT/bin/judo-20051212.jar"</code>

### Building with version 2 website

This is the later one, so let us start here. The key to remember is that _articles_, _references_ and _book chapters_ are all in the module of <code>projects/new_site</code> already.

1. <code>cd $JUDOROOT/projects/judoscript-0.9/buildtools/</code>, and run <code>judo all</code>.
   * Build the software and create the executable jar.
   * Build all examples.
2. <code>cd $JUDOROOT/projects/jamaica-1.0/buildtools/</code>, and run <code>judo build</code>.
3. <code>cd $JUDOROOT/projects/new_site/buildtools/</code>, and run <code>judo build</code>, all options.
4. <code>cd $JUDOROOT/projects/judoscript-0.9/buildtools/</code>, and run <code>judo all</code>, "Ship It!".
5. <code>cd $JUDOROOT/projects/new_site/buildtools/</code>, and run <code>judo build</code>, "Build site juspt contnet", again.

### Building with version 1 website

This is the earlier one. The key to remember is that _articles_ and _references_ are included in the the module of <code>projects/judoscript-0.9</code>, while _book chapters_ are in a separate module, <code>projects/bookwriting</code>.

1. <code>cd $JUDOROOT/projects/judoscript-0.9/buildtools/</code>, and run <code>judo all</code>.
   * Build the software and create the executable jar.
   * Build all documentations, including articles and references.
   * Build all examples.
2. <code>cd $JUDOROOT/projects/jamaica-1.0/buildtools/</code>, and run <code>judo build</code>.
3. <code>cd $JUDOROOT/projects/bookwriting/buildtools/</code>, and run <code>judo build</code>.
4. <code>cd $JUDOROOT/projects/judoscript.com/buildtools/</code>, and run <code>judo build</code>, all options.
5. <code>cd $JUDOROOT/projects/judoscript-0.9/buildtools/</code>, and run <code>judo all</code>, "Ship It!".
6. <code>cd $JUDOROOT/projects/judoscript.com/buildtools/</code>, and run <code>judo build</code>, "Build Pages", again.

## The live sites in this Museum

* The live [version 1 site](judoscript.com-v1)
* The live [version 2 site](judoscript.com-v2)
