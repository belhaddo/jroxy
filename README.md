# JRoxy

Java reverse proxy project 


#### To be able to call the service using url (my-service.my-company.com)
You need to add this entries to dns config in '/etc/hosts'

127.0.0.1   my-service.my-company.com

127.0.0.1   my-service-a.my-company.com

then refresh dns:
 - for macos : sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder