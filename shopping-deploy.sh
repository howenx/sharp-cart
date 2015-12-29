#!/usr/bin/expect
set timeout 10
set src_file [lindex $argv 0]
set des_file [lindex $argv 1]
set password hanmimei2015Cn
set password2 hanmimei2016Com
spawn scp $src_file deploy@121.43.186.42:~/$des_file
expect {
 "(yes/no)?"
  {
	  send "yes\n"
	  expect "*assword:" { send "$password\n"}
  }
  "*assword:"
  {
	  send "$password\n"
  }
}
expect "100%"
spawn ssh deploy@121.43.186.42
expect {
	"yes/no" { send "yes\n";exp_continue }
	"*assword:" { send "$password\n" }
}
set prompt ":|#|\\\$"
interact -o -nobuffer -re $prompt return

send "scp $des_file deploy@192.168.6.11:~\n" 
interact -o -nobuffer -re $prompt return
expect {
 "(yes/no)?"
  {
	  send "yes\n"
	  expect "*assword:" { set timeout 300;send "$password\n"}
  }
  "*assword:"
  {
	  send "$password2\n"
  }
}
expect "*100%*"
interact -o -nobuffer -re $prompt return
send "rm -rf $des_file\n" 
send "ssh deploy@192.168.6.11\n" 
expect {
	"yes/no" { send "yes\n";exp_continue }
	"*assword:" { set timeout 300;send "$password2\n" }
}
interact -o -nobuffer -re $prompt return
set timeout 300;send "unzip $des_file\n"
expect eof
