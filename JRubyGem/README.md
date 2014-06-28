Sikuli JRubyGem 2014 (version 1.1.x)
===

Implements the Ruby gem for using sikulix.<br />
(Requires SIKULIXAPI_JAR with full path of `sikulixapi.jar`)

## Where to get
It is possible to build it from sources using mvn or gem.
Run in this path

```
mvn
```

Or run

```
gem build sikulix.gemspec
```

Or download sukulix.gem from https://launchpad.net/sikuli

## Install

Run

```
gem install sikulix.gem --local
```

## How to use

### Prerequisits
* Download [sikulix-setup](https://launchpad.net/sikuli) and install Pack 2 ('I want to develop in Java,JRuby....')
* Download and install [JRuby](http://jruby.org/)

### Running scripts

* Set SIKULIXAPI_JAR environment variable to sikulixapi.jar with full path

  ```
  Windows: set SIKULIXAPI_JAR=c:\...\...\..\sikulixapi.jar
  Linux: export SIKULIXAPI_JAR=/.../.../../sikulixapi.jar
  ```
* Create ruby script that includes following strings

    ```ruby
    require 'sikulix'
    include SikuliX4Ruby

    # place your code here

    ```
* Run it with jruby

## Special for Ruby

* After 'include SikuliX4Ruby' it is possible to use “undotted” methods. E.g. click(), exists(), etc in global context.
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
