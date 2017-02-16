# GTFS-simulation-web

The application reads gtfs transport schedule data from an unzipped directory and serve them via a REST API.
The play/scala source code in this repository handle the backend application, although the packageed archived contains both sback and front end components..

A [blog]() post explains the whole story

Enjoy!

## Download and unzip gtfs data

Being focus on Switzerland by the JavaScript application, head to http://gtfs.geops.ch/ and download one of the feeds (either the *complete* set or a more tailored)Unzip it into a directory (*e.g.* $HOME/tmp/gtfs/)

## Packaged distribution

You just want to run the app?
Hopefully, an archive has been packaged, with the REST backend and the JavaScript application.
You only need Java 8 installed.

Download the latest zip archive from [here](https://extranet.octo.com/oft/viewfile.php?fileid=5b6839ea74827b220b8e4637c5fd1daf) and unzip it.
From within the extracted directory:

    bin/gtfs-simulation-play -Dschedule.gtfs.path=$HOME/tmp/gtfs/ -Dschedule.date=20170213

With large data, such as the full Swiss schedule for one year, it can take up to 2.5 minutes to start on a Macbok pro.
Then head to http://localhost:9000 and enjoy!

## Development

### code & run

The stack uses activator

    ./activator ~run
    ./activator ~test

### Distribution

A packaged zip is built in `target/universal`. This is the file to be downloaded in the previsouly mentioned link

    ./activator dist

### Continuous buid

SImply with travis https://travis-ci.org/alexmasselot/gtfs-simulation-play
