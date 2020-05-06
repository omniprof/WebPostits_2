# WebPostits_2
 Provide unconference marketplace/timetable, sourced from QiqoChat. 
 The project layout is as required by Elastic Beanstalk
 (https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-tomcat-platform-directorystructure.html). 

There are two changes that I have made. The first is that I rewrote the threading routine in ApplicationStartUp. You should never use bare Threads in a web app. It has been replaced with a ScheduledExecutorService. This eliminates the error prone Thread.sleep.

The second came about because of a problem with threading, either the original or my version. That was why I moved the program to a Maven build. In its original format the starting of the scraping thread was hit or miss. I don't have a clue why. Once converted to Maven the scraping thread always ran. It wasn't the ScheduledExecutorService but something in the dependencies, I believe, is at fault. The Maven build eliminated the issue. 

Of course now that its a Maven build the war file does not conform to AWS so it won't run on AWS. It runs fine on Tomcat in its container. Given more time I could write a script to generate the war in a AWS acceptable format. In the meantime I will not recommend AWS to anyone, but thats just me.

The only changes are in ApplicationStartUp so it can be copied to an AWS compatible version. 
 

## Docker - New

This version is Maven based. It will load into any IDE and can be run in that IDE or thru Docker.

The build.sh has been removed as the program is required to be compiled and packaged using Maven in your IDE.

To use Docker:

For Mac or Linux this command line will still work:
docker build -t jalba . && docker run -p 80:8080 jalba

On Windows use dodock.bat

Once it starts you can see it at http://localhost/jalba

To stop the container and remove it you enter:
docker container stop $(docker container ps -q)
This works for Mac, Linux or Windows. For an unknown reason it will not work in a Windows batch file. I have not tried it in a shell script.


## Docker - Original

 build.sh creates the ROOT.war, which runs on Tomcat 8.5, and is what is uploaded to AWS. 

To run the application locally in a dockerized tomcat, run build.sh to get the war file built, then run 
```
docker build -t jalba .
```
to build the image and with
``` 
docker run -p 80:8080 jalba
```
you can start the tomcat server with the deployed app available at [http://localhost/jalba/](http://localhost/jalba/)
