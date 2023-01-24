- cd /home/farhad/apps/spring-integration-sample/spring-flo-old/spring-flo/samples/spring-flo-si
- mvn spring-boot:run
- then open http://localhost:8082.
- In the Spring Integration Graph Endpoint field enter the url for the spring integration data, for 
  example: http://localhost:8080/integration and the graph should load.


Once the graph is loaded you can drag nodes around to adjust the layout. (Press the Read-Only button to prevent moving nodes around). Hovering 
over a node will show a tool tip with more information for that element. If you hover over a channel you will see many stats about traffic 
flowing over that channel:

It is possible to select one of those stats of interest and have it shown directly on the graph. Simply select what you are interested in and 
enter the name of that stat in the Link label path field at the top. The values for that stat will then be shown on the links between graph 
elements

If you enter a Refresh rate (minimal allowed is 250ms) then that stat will actually update on the graph at that rate with a small animation 
indicating where on the graph changes in value are occurring.
