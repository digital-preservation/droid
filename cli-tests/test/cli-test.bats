#!/usr/bin/env bats

setup_file() {
  mkdir -p /code/cli-tests/binary
  cd /code/cli-tests/binary
  unzip -q -o /code/droid-binary/target/droid-binary-*bin.zip
  cd /code
  /code/cli-tests/binary/droid.sh -d
}

setup() {
  DROID_BIN=/code/cli-tests/binary/droid.sh
  bats_load_library bats-support
  bats_load_library bats-assert
  mkdir -p folder/subfolder
  echo test | tee folder/file1.txt folder/file2.csv folder/file2.xml folder/subfolder/file1.txt > /dev/null
}

@test "shows the help text" {
  run $DROID_BIN --help
  assert_output --partial 'usage: droid [options]'
}

@test "clean removes folders from droid home" {
  $DROID_BIN --clean
  run ls ~/.droid6
  assert_output -p 'export_templates'
  assert_output -p 'filter_definitions'
  assert_output -p 'logs'
  assert_output -p 'report_definitions'
  refute_output -p 'container_sigs'
  refute_output -p 'signature_files'
  refute_output -p 'profiles'
  refute_output -p 'profile_templates'
  refute_output -p 'droid.properties'
  refute_output -p 'log4j2.properties'
  refute_output -p 'tmp'
}

@test "list the reports" {
  run $DROID_BIN -l
  assert_output -p "Report:	'Total unreadable folders'"
  assert_output -p "Report:	'File count and sizes by month last modified'"
  assert_output -p "Report:	'Total count of files and folders'"
  assert_output -p "Report:	'File count and sizes by year last modified'"
  assert_output -p "Report:	'File count and sizes by file format PUID'"
  assert_output -p "Report:	'Comprehensive breakdown'"
  assert_output -p "Report:	'File count and sizes by year and month last modified'"
  assert_output -p "Report:	'File count and sizes'"
  assert_output -p "Report:	'File count and sizes by file extension'"
  assert_output -p "Report:	'File count and sizes by mime type'"
  assert_output -p "Report:	'Total unreadable files'"
}

@test "check for signature updates" {
  run $DROID_BIN -c
  assert_output -p 'Container signature update Version 20250925 is available'
  assert_output -p 'Binary signature update Version 121 is available'
}

@test "download signature updates" {
  run $DROID_BIN -d
  assert_output -p 'Signature update version 20,250,925 has been downloaded'
  assert_output -p 'Signature update version 121 has been downloaded'
}

@test "list signature files" {
  run $DROID_BIN -X
  assert_output -p "Type: Binary Version:  121  File name: DROID_SignatureFile_V121.xml"
  assert_output -p "Type: Container Version:  20250925  File name: container-signature-20250925.xml"
}

@test "display signature file" {
  run $DROID_BIN -x
  assert_output -p "Type: Container Version:  20250925  File name: container-signature-20250925.xml"
  assert_output -p "Type: Binary Version:  121  File name: DROID_SignatureFile_V121.xml"
}

@test "set signature file" {
  $DROID_BIN -X
  curl https://cdn.nationalarchives.gov.uk/documents/DROID_SignatureFile_V109.xml -o $HOME/.droid6/signature_files/DROID_SignatureFile_V109.xml
  run $DROID_BIN -s 109
  assert_output "Default signature file updated. Version: 109  File name: DROID_SignatureFile_V109.xml"
}


@test "no profile mode folder with json output" {
  run $DROID_BIN -Nr folder --json
  assert_output -p '{"FILE_PATH":"/code/folder/file2.xml"}'
  assert_output -p '{"FILE_PATH":"/code/folder/file2.csv","PUID":"x-fmt/18"}'
  assert_output -p '{"FILE_PATH":"/code/folder/file1.txt","PUID":"x-fmt/111"}'
  rm -rf folder
}

@test "no profile mode folder with extension filter and csv output" {
  run $DROID_BIN -Nr folder -Nx csv
  assert_output -p /code/folder/file2.csv,x-fmt/18
  rm -rf folder
}

@test "no profile mode folder with extension filter and json output" {
  run $DROID_BIN -Nr folder -Nx csv --json
  assert_output -p '{"FILE_PATH":"/code/folder/file2.csv","PUID":"x-fmt/18"}'
  rm -rf folder
}

@test "will find dev signature in mocked signature files" {
  $DROID_BIN -Nr /code/cli-tests/testfiles/test.test -Ns /code/cli-tests/testfiles/binary-signature.xml -Nc /code/cli-tests/testfiles/container-signature.xml -o mock.csv
  run cat mock.csv
  assert_line -n 1 /code/cli-tests/testfiles/test.test,dev/1
  rm mock.csv
}

@test "create and export a profile without recursion to csv" {
  $DROID_BIN -a folder -p profile.droid
  $DROID_BIN -p profile.droid -E out.csv
  run cat out.csv
  assert_line -n 1 -p folder
  assert_line -n 2 -p folder/file1.txt
  assert_line -n 3 -p folder/file2.csv
  assert_line -n 4 -p folder/file2.xml
  assert_line -n 5 -p folder/subfolder
  refute_line -p folder/subfolder/file1.txt
}

@test "create and export a profile with recursion to csv" {
  $DROID_BIN -a folder -R -p profile.droid
  $DROID_BIN -p profile.droid -E out.csv
  run cat out.csv
  assert_line -n 1 -p folder
  assert_line -n 2 -p folder/file1.txt
  assert_line -n 3 -p folder/file2.csv
  assert_line -n 4 -p folder/file2.xml
  assert_line -n 5 -p folder/subfolder
  assert_line -n 6 -p folder/subfolder/file1.txt
}

@test "create and export a profile without recursion to json" {
  $DROID_BIN -a folder -p profile.droid
  $DROID_BIN -p profile.droid -E out.json --json
  assert_equal $(jq length out.json) 5
  assert_equal $(jq -r '.[0].FILE_PATH' out.json) $PWD/folder
  assert_equal $(jq -r '.[1].FILE_PATH' out.json) $PWD/folder/file1.txt
  assert_equal $(jq -r '.[2].FILE_PATH' out.json) $PWD/folder/file2.csv
  assert_equal $(jq -r '.[3].FILE_PATH' out.json) $PWD/folder/file2.xml
  assert_equal $(jq -r '.[4].FILE_PATH' out.json) $PWD/folder/subfolder
  rm -rf folder out.json profile.droid
}

@test "create and export a profile with recursion to json" {
  $DROID_BIN -a folder -R -p profile.droid
  $DROID_BIN -p profile.droid -E out.json --json
  assert_equal $(jq length out.json) 6
  assert_equal $(jq -r '.[0].FILE_PATH' out.json) $PWD/folder
  assert_equal $(jq -r '.[1].FILE_PATH' out.json) $PWD/folder/file1.txt
  assert_equal $(jq -r '.[2].FILE_PATH' out.json) $PWD/folder/file2.csv
  assert_equal $(jq -r '.[3].FILE_PATH' out.json) $PWD/folder/file2.xml
  assert_equal $(jq -r '.[4].FILE_PATH' out.json) $PWD/folder/subfolder
  assert_equal $(jq -r '.[5].FILE_PATH' out.json) $PWD/folder/subfolder/file1.txt
  rm -rf folder out.json profile.droid
}

@test "file extension filter filters rows when creating a profile with json output" {
  $DROID_BIN -a folder -ff file_ext=txt -p profile.droid
  $DROID_BIN -p profile.droid -E out.json --json
  assert_equal $(jq length out.json) 3
  assert_equal $(jq -r '.[0].FILE_PATH' out.json) $PWD/folder
  assert_equal $(jq -r '.[1].FILE_PATH' out.json) $PWD/folder/file1.txt
  assert_equal $(jq -r '.[2].FILE_PATH' out.json) $PWD/folder/subfolder
  rm -rf folder out.json profile.droid
}

@test "file extension filter filters rows when creating a profile with recursion and json output" {
    $DROID_BIN -a folder -R -ff file_ext=txt -p profile.droid
    $DROID_BIN -p profile.droid -E out.json --json
    assert_equal $(jq length out.json) 4
    assert_equal $(jq -r '.[0].FILE_PATH' out.json) $PWD/folder
    assert_equal $(jq -r '.[1].FILE_PATH' out.json) $PWD/folder/file1.txt
    assert_equal $(jq -r '.[2].FILE_PATH' out.json) $PWD/folder/subfolder
    assert_equal $(jq -r '.[3].FILE_PATH' out.json) $PWD/folder/subfolder/file1.txt
    rm -rf folder out.json profile.droid
}

@test "file extension filter filters rows when creating a profile with csv output" {
  $DROID_BIN -a folder -ff file_ext=txt -p profile.droid
  $DROID_BIN -p profile.droid -E out.csv
  run cat out.csv
  assert_line -n 1 -p folder
  assert_line -n 2 -p folder/file1.txt
  assert_line -n 3 -p folder/subfolder
  rm -rf folder out.json profile.droid
}

@test "file extension filter filters rows when creating a profile with recursion and csv output" {
  $DROID_BIN -a folder -R -ff file_ext=txt -p profile.droid
  $DROID_BIN -p profile.droid -E out.csv
  run cat out.csv
  assert_line -n 1 -p folder
  assert_line -n 2 -p folder/file1.txt
  assert_line -n 3 -p folder/subfolder
  assert_line -n 4 -p folder/subfolder/file1.txt
  refute_line -p folder/file2.csv
  refute_line -p folder/file2.xml
  rm -rf folder out.json profile.droid
}

@test "warc files will not be expanded" {
  TEST_FILES=/code/droid-command-line/src/test/resources/testfiles
  run $DROID_BIN -Nr $TEST_FILES/expanded.warc
  assert_line -p $TEST_FILES/expanded.warc,fmt/1355
  refute_line -p fmt/96
  refute_line -p test.js,x-fmt/423
  refute_line -p test.html,fmt/96
}

@test "warc files will be expanded" {
  TEST_FILES=/code/droid-command-line/src/test/resources/testfiles
  $DROID_BIN -W -Nr $TEST_FILES/expanded.warc -o out.csv
  run sort out.csv
  assert_output -p $TEST_FILES/expanded.warc,fmt/1355
  assert_line -p warc:$TEST_FILES/expanded.warc!/,fmt/96
  assert_line -p warc:$TEST_FILES/expanded.warc!/test.html,fmt/96
  assert_line -p warc:$TEST_FILES/expanded.warc!/test.js,x-fmt/423
  rm out.csv
}

@test "archive files will not be expanded" {
  touch test.txt
  zip test.zip test.txt
  tar -cf test.tar.gz test.txt
  $DROID_BIN -Nr test.tar.gz --json -o out-tar.json
  $DROID_BIN -Nr test.zip --json -o out-zip.json
  assert_equal $(jq length out-tar.json) 1
  assert_equal $(jq length out-zip.json) 1
  assert_equal $(jq -r '.[0].PUID' out-tar.json) x-fmt/265
  assert_equal $(jq -r '.[0].PUID' out-zip.json) x-fmt/263
  rm -f test.txt test.zip test.tar.gz out-tar.json out-zip.json
}

@test "archive files will be expanded" {
  touch test.txt
  zip test.zip test.txt
  tar -cf test.tar.gz test.txt
  $DROID_BIN -Nr test.tar.gz -A --json -o out-tar.json
  $DROID_BIN -Nr test.zip -A --json -o out-zip.json
  assert_equal $(jq length out-tar.json) 2
  assert_equal $(jq length out-zip.json) 2
  assert_equal $(jq -r '.[0].PUID' out-tar.json) x-fmt/265
  assert_equal $(jq -r '.[1].PUID' out-tar.json) x-fmt/111
  assert_equal $(jq -r '.[0].PUID' out-zip.json) x-fmt/263
  assert_equal $(jq -r '.[1].PUID' out-zip.json) x-fmt/111
  rm -f test.txt test.zip test.tar.gz
}

@test "comprehensive report will be generated" {
  echo test > file1.txt
  $DROID_BIN -a file1.txt -p profile.droid
  $DROID_BIN -r report.txt -n 'Comprehensive breakdown' -t text -p profile.droid
  run cat report.txt
  assert_output -p 'File count and sizes per year and month last modified'
  assert_output -p 'x-fmt/111'
  rm file1.txt profile.droid report.txt
}
