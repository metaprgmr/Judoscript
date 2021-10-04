                     Embedding JudoScript in Java


JudoScript provides a language engine that can be used in Java software.
It also supports the BSF (Bean Scripting Framework) interface, allowing
any software packages with BSF support to use JudoScript.


- CallTest.java     uses JudoScript engine API to create a JudoScript
                    function and use it from Java.

- BSFCallTest.java  uses JudoScript BSF API to create a JudoScript
                    function and use it from Java.

- randgen.judo      All three constitute an experiment.
  BSFTest.java      randgen.judo implements a simple AWT GUI.
  EmbedTest.java    It takes its Frame object from Java and
                    pass back the Frame's title text into Java.
                    The Java program actually launches the GUI.

                    The flow for Java code is:
                      1. Get the engine
                      2. Set to engine: "frame" = new Frame();
                      3. Engine run the JudoScript code,
                         which sets to engine: "title" = some text.
                      4. Gets "title" and set to "frame".
                      5. frame.show().
                    * BSFTest.java   - BSF version,
                    * EmbedTest.java - JudoScript engine API version.

