# fs-dumper

(c) 2019 Marek Rychly (rychly@fit.vutbr.cz)

A tool to dump a file-system structure and store it into a ZIP file.

## Usage

~~~
# create new archive recursively including all files in the source dir and their attributes in comments
java -jar fs-dumper.jar c <zip-file> <source-dir-to-pack>
~~~

~~~
# create the same directory structure as the source dir their where the files will contain lists of properties storing attributes of coresponding files in the source dir
java -jar fs-dumper.jar a <dir-to-save-attrs> <source-dir-to-analyze>
~~~~

## Build

~~~
./gradlew build
ls -l ./build/libs/*.jar
~~~

## Acknowledgements

*This work was supported by the Ministry of the Interior of the Czech Republic as a part of the project Integrated platform for analysis of digital data from security incidents VI20172020062.*
