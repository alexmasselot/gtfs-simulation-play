# GTFS-simulation-web

The application reads gtfs transport schedule data from an unzipped directory and serve them via a REST API.
The play/scala source code in this repository handle the backend application, although the packageed archived contains both sback and front end components..

A [blog]() post explains the whole story

Enjoy!

## Download and unzip gtfs data

Being focus on Switzerland by the JavaScript application, head to http://gtfs.geops.ch/ and download one of the feeds.
unzip it into a directory (*e.g.* $HOME/tmp/gtfs/)

## Packaged distribution

You just want to run the app?
Hopefully, an archive has been packaged, with the REST backend and the JavaScript application.
You only need Java 8 installed.

Download the latest zip archive from [target/universal/](target/universal/) and unzip it.
From within the extracted directory

    bin/gtfs-simulation-play -Dschedule.gtfs.path=$HOME/tmp/gtfs/ -Dschedule.date=20170213


## Development

### code & run

The stack uses activator

    ./activator ~run
    ./activator ~test


### Continuous build