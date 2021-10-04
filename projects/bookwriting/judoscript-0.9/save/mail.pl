#!/usr/bin/perl -w

use strict;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Response;

my $ua = new LWP::UserAgent;

my $request = new HTTP::Request('GET', 'http://system:zgumby1234@cib-app1.bea.com:8080/index.jsp' );
my $response = $ua->request($request);
if ($response->is_success) {
     exit 0;
} else {
     open(MAIL, "| /usr/lib/sendmail cib_support_alias");
     print MAIL "From: root\@cib-app1\n";
     print MAIL "Subject: CIB application problem \n\n";
     close(MAIL);
     exit 1;
}
