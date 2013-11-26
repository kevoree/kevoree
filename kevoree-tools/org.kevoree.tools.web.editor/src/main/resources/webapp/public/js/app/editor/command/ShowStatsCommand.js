define(
    [
        'util/ModelHelper'
    ],

    function (ModelHelper) {

        function ShowStatsCommand() {}

        ShowStatsCommand.prototype.execute = function (editor) {
            var model = editor.getModel();

            var nbGrps              = 0,
                nbGrpInstances      = 0,
                nbComps             = 0,
                nbCompsInstances    = 0,
                nbNodes             = 0,
                nbNodesInstances    = 0,
                nbChans             = 0,
                nbChansInstances    = 0;

            // get model statistics
            if (model != null && model != undefined) {
                var libz = ModelHelper.getLibraries(model);
                for (var i=0; i < libz.length; i++) {
                    var compz = libz[i].components;
                    for (var j=0; j < compz.length; j++) {
                        switch (compz[j].type) {
                            case 'ComponentType':
                                nbComps++;
                                break;
                            case 'NodeType':
                                nbNodes++;
                                break;
                            case 'GroupType':
                                nbGrps++;
                                break;
                            case 'ChannelType':
                                nbChans++;
                                break;
                            default:
                                break;
                        }
                    }
                }

                // retrieve instances count from model
                nbGrpInstances      = model.getGroups().size();
                nbCompsInstances    = getComponentInstancesCount(model);
                nbNodesInstances    = model.getNodes().size();
                nbChansInstances    = model.getHubs().size();
            }

            // set popup content
            $('#stats-popup-content').html(
                "<h6>Type definitions:</h6>" +
                "<table class='table'>" +
                createHTMLRow("Group Type", nbGrps) +
                createHTMLRow("Component Type", nbComps) +
                createHTMLRow("Node Type", nbNodes) +
                createHTMLRow("Channel Type", nbChans) +
                "</table>"+

                "<h6>Instances:</h6>" +
                "<table class='table'>" +
                createHTMLRow("Group Instances", nbGrpInstances) +
                createHTMLRow("Component Instances", nbCompsInstances) +
                createHTMLRow("Node Instances", nbNodesInstances) +
                createHTMLRow("Channel Instances", nbChansInstances) +
                "</table>"
            );

            // show popup
            $('#stats-popup').modal({show: true});
        }

        return ShowStatsCommand;

        function createHTMLRow(tag, value) {
            return "<tr>" +
                     "<td class='span8'>"+tag+"</td>"+
                     "<td class='span4'>"+
                        "<div class='pull-right'>"+ value +"</div>"
                     "</td>"+
                   "</tr>";
        }

        function getComponentInstancesCount(model) {
            var count = 0,
                nodes = model.getNodes();

            for (var i=0; i < nodes.size(); i++) count += nodes.get(i).getComponents().size();

            return count;
        }
    }
);