#!/bin/bash 

if [ $UID -ne 0 ] ; then 
		echo "Please execute command as root"
		exit 1
fi

INSTALL_PATH="/opt/youtube-downloader" 

if [ ! -d $INSTALL_PATH ] ; then 
		mkdir $INSTALL_PATH 
fi

install -m 644 target/youtube-downloader.jar $INSTALL_PATH/youtube-downloader.jar
install -m 755 ydl $INSTALL_PATH/ydl
update-alternatives --install /usr/local/bin/ydl ydl $INSTALL_PATH/ydl 100


