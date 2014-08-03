Sikuli JRubyGem 2014 (version 1.1.x)
===

Implements the Ruby gem for using sikulix.<br />
(Requires environment variable SIKULIXAPI_JAR <br />with full path to `sikulixapi.jar` version 1.1.0-Beta4+)

## Where to get
It is possible:

* to download `sikulix` from http://rubygems.org/sikulix
* build it from sources using `mvn` or `gem`.
  Run in this path

  ```
  mvn
  ```

  Or run (needs a valid JRuby/gem environment)

  ```
  jgem build sikulix.gemspec
  ```

## Install

needs a valid JRuby/gem environment

* Install from rubygems:

  ```
  jgem install sikulix
  ```

* Local install:

  ```
  jgem install sikulix-x.x.x.x.gem --local
  ```

## How to use

### Prerequisits
* Download [sikulixsetup.jar](https://launchpad.net/sikuli/sikulix/1.1.0) and install Pack 2 ('I want to develop in Java,JRuby....')<br />**Until availability** of a stable version 1.1.0-Beta4<br />you might download a ready to use `sikulixapi-1.1.0-Beta4.jar` from [nightly build](http://nightly.sikuli.de)
* Download and install [JRuby](http://jruby.org/)

### Running scripts

* Set SIKULIXAPI_JAR environment variable to sikulixapi.jar with full path<br />examples:

  ```
  Windows: set SIKULIXAPI_JAR=c:\...\...\..\sikulixapi-1.1.0-Beta4.jar
  Mac/Linux: export SIKULIXAPI_JAR=/.../.../../sikulixapi-1.1.0-Beta4.jar
  ```
* Create ruby script that includes following strings

    ```ruby
    require 'sikulix'
    include Sikulix

    # place your code here

    ```
* Run it with jruby

## Special for Ruby

* After 'include Sikulix' it is possible to use “undotted” methods. E.g. click(), exists(), etc in global context.
* Registration of hot-keys:

    ```ruby
    addHotkey( Key::F1, KeyModifier::ALT + KeyModifier::CTRL) do
      popup 'hallo', 'Title'
    end
    ```
* Registration of events:

    ```ruby
    onAppear("123.png") { |e| popup 'hi', 'title' }
    # ...
    observe 10
    ```
* Alternative events registration:

    ```ruby
    # event with lambda with a parameter
    hnd = ->(e) {popup(e.inspect, 'hi!')}
    onAppear "123.png", &hnd
    # ...
    observe
    ```
* Creating objects of Sikuli classes without explicit constructor

    ```ruby
    ptn = Pattern("123.png").similar(0.67)
    ```

* Possibility to enumerate array

    ```ruby
    findAll("123.png").each do |obj|
      puts obj.getTarget.toString()
    end
    ```

## Other docs

**Usage docs now on [ReadTheDocs](http://sikulix-2014.readthedocs.org/en/latest/#) (work in progress)**
