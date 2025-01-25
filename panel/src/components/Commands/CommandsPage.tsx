import {Tabs, TabsContent, TabsList, TabsTrigger} from "../../../@shadcn/components/ui/tabs.tsx";
import AllCommandList from "./commands/AllCommandList.tsx";
import TemplateListPane from "../Templates/TemplateList/TemplateListPane.tsx";
import UserCommandList from "./commands/UserCommandList.tsx";
import "./CommandPage.css"
import TemplateList from "./templates/TemplateList.tsx";

export default function CommandsPage() {
  return <Tabs defaultValue="commands" >
    <TabsList className="commandTabList">
      <TabsTrigger value="commands">User-Commands</TabsTrigger>
      <TabsTrigger value="templates">Templates</TabsTrigger>
      <TabsTrigger value="trigger">Internal Commands</TabsTrigger>
      <TabsTrigger value="templates2">Templates 2</TabsTrigger>
    </TabsList>
    <TabsContent value="commands">
      <UserCommandList/>
    </TabsContent>
    <TabsContent value="templates">
      <TemplateListPane/>
    </TabsContent>
    <TabsContent value="trigger">
      <AllCommandList/>
    </TabsContent>
    <TabsContent value="templates2">
      <TemplateList/>
    </TabsContent>
  </Tabs>
}