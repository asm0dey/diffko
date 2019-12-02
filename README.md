# Diffko

From-scratch implementation of Myers diff algorithm http://www.xmailserver.org/diff2.pdf

## About implementation

I've tryed hard to leave variable names as in paper mentioned above.
They partially are unreadable and partially break Java/Kotlin conventions,
but I've decided that one who will decide to compare implementation with
paper will have a little bit less WTF/s.

## Build

```
./gradlew build
```

Build jars will be located in `build/libs` and distributions (with run scripts) will be in build/distributions/

## Run

```
java -jar diffko-all.jar --help
```

This command will print usage information like this:

```
Usage: diffko [OPTIONS]

Options:
  --text, --color     Color mode (tries to highlight changes in red and green)
                      ot Text mode (highlight canges with [[ ]] and << >>)
  -s, --source FILE   Original file
  -r, --revised FILE  Revised file
  -h, --help          Show this message and exit
```

By default `--text` mode is selected so you only need to point software to source and revised files.

## Test coverage

I've did my best to cover algorithm itself with tests. Currently JetBrains IDEA shows full coverage of
`MyersDiff` and `DiffPrinter` classes (excluding unreachable statements)

Less interesting `Main.kt` and `Cli.kt` files are covered also.

Part of testing is property-based testing which assures that there are no (currently known) strings, which
causes diff algorithm to fail.
