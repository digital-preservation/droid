FROM lscr.io/linuxserver/webtop:ubuntu-xfce
RUN apt-get install maven git
RUN git clone https://github.com/digital-preservation/droid.git && \
    git checkout ui-tests-for-droid && \
    cd droid && \
    mvn clean install -DskipTests -Dcheckstyle.skip=true -Ddependency-check.skip=true && \
    mvn test -Dtest="uk.gov.nationalarchives.droid.gui.AssertJTest" -pl droid-swing-ui
