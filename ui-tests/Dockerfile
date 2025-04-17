FROM lscr.io/linuxserver/webtop:ubuntu-xfce
WORKDIR droid
RUN apt-get update && \
    apt-get -y install maven git openjdk-21-jdk && \
    mkdir -p /home/test/ /home/profiles && \
    for i in $(seq 1 10000); do echo test > "/home/test/file-$i.txt"; done && \
    mkdir -p /home/test/test-subfolders/subfolder1 && \
    echo test > /home/test/test-subfolders/file1.txt && \
    echo test > /home/test/test-subfolders/subfolder1/file2.txt
COPY . .
CMD ./ui-tests/ui-test-startup.sh

