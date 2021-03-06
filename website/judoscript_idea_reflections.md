# Reflections on _the Judoscript Idea_

Is _the Judoscript idea_ a good one? This question is centered around domain support. There are two ways to handle domain support: declarative and API based. Judoscript aggressively embraces the declarative approach, while most others employ APIs only.

Some of Judoscript's domain support can, arguably, be implemented as functions. This is especially true if named parameters are support for function calls. Imagine that Java eventually support named parameters for invocation, then any Java-scripting-capable languages would be able to use in ways almost like those of Judoscript.

Some of Judoscript's domain support is beyond mere APIs. The SAX-style XML and SGML processing is a good example, where in pure Java, even with a scraping library,  one would have to implement a few Java interfaces and some house-keeping code to achieve the same. Another one is JDBC scripting. With Judoscript, SQL statements can be written plainly, possibly with embedded expressions, and natural-looking parameter binding; it is so much more intuitive and readable than using plain JDBC API, which is still possible in Judoscript using regular Java scripting.

Declarative programming features are not expandable. Once the syntax is defined, it is stuck until future releases of the language. Likewise, a new domain support requires the language and its parser to be expanded. On the contrary, API-based features can be extended and expanded by writing new libraries without having to upgrade the language itself.

One of the reasons that not many Judoscript-like languages exist is that, the compiler is far more complex than _normal_ programming languages. Even for scripting languages, which typically have quite a few syntactic sugars, their irregularity is still very limited (to avoid the _write-only_ syndrome). The parser for Judoscript, on the other hand, has many sub-parsers within for domain-specific sub-languages; and these sub-languages need to support embedding Judoscript expressions from the main parser. This makes the parser very difficult to write.

Another concern is usability and learning curve. For an API-based language, at least the form of invocation is consistent; coders would just consult the API specifications to use. For domain features in Judoscript-like languages, coders would have to consult the sub-language manuals frequently. One can argue that syntactic sugar is similar, but the amount of syntactic sugar is limited and relatively easy to remember; specific domain support may appear obscure at first encounter, especially if the language designer has chosen something less than perfect.

Declarative programming within an algorithmic base language certainly has its power. The question is how far a language should go. A common approach in practical language designs is to adopt a small number of very carefully chosen domains with declarative programming, and leave others to APIs. Database is always a prominent candidate, such as in SQR, Oracle's Pro*C, Salesforce's Apex, etc.; this becomes especially powerful if the language owner also controls the embedded query languages. That is why it is common for software vendors to include their own proprietary languages.

As for a general purpose language with many domain-specific support, Judoscript remains the only one to date. Groovy and its maker, JavaSoft, have undergone drastic changes just a few years later; if the author of Judoscript could foresee such changes, perhaps he would have continued Judoscript's growth path, and we would have data to convince ourselves, either way, whether _the Judoscript idea_ is a good one or not.

## Quick Links

* [The Story and Philosophy of Judoscript](./story_philosophy.md)
* [Introduction to the Judoscript Language](./intro_to_judoscript.md)
* Live [reconstructed version 2 judoscript.com site](judoscript.com-v2)
* Live [reconstructed version 1 judoscript.com site](judoscript.com-v1)
* [Building the language software and website](./build_judoscript_website.md)
* The [Home page](./)
