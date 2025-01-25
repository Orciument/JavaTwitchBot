import "../common/ListView.css"
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "../../../../@shadcn/components/ui/table.tsx";
import {Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle} from "../../../../@shadcn/components/ui/sheet.tsx";
import {useState} from "react";
import Loader from "../../../common/LoadingSpinner/Loader.tsx";
import useData from "../../../common/useData.ts";
import {Button} from "../../../../@shadcn/components/ui/button.tsx";
import {Input} from "../../../../@shadcn/components/ui/input.tsx";
import {Search} from "lucide-react";
import WarningBox from "../../../common/warning/WarningBox.tsx";
import {Template} from "./Template.ts";
import TemplateForm from "./TemplateForm.tsx";

export default function TemplateList() {
  const [searchBox, setSearchBox] = useState<string>("")
  const {data, loading, sendData} = useData<Template[]>("/template/all?search=" + encodeURIComponent(searchBox), "Templates", [])
  const [openTemplate, setOpenTemplate] = useState<Template | undefined>(undefined)

  function handleSearch() {}

  if (loading) {
    return <Loader/>
  }

  if (data == undefined) {
    return "ERROR" //TODO
  }

  return <div className="commandList">
    <WarningBox>TODO This Page lists all commands that are registered in the Bot, this includes internal command that are
      used for various other features like giveaways and management features for mods. <br/>
      Most of these Commands don't have a template (text output), instead they call an internal function of the Bot that
      can then process the command action and may then respond with different or multiple templates. <br/>
      If you delete a command that was registered automatically it will be regenerated with the next startup. Instead,
      if you want to disable the command disable all its patterns. You can still use the same patterns in other
      commands.
    </WarningBox>
    <div className="actionBar">
      <div className="searchBox">
        <Input placeholder="Search for a Template" value={searchBox} onChange={event => setSearchBox(event.target.value)}/>
        <Button onClick={() => handleSearch()}><Search/></Button>
      </div>
    </div>
    <Table>
      <TableHeader>
        <TableHead>Name/Id</TableHead>
        <TableHead>Template Text</TableHead>
      </TableHeader>
      <TableBody>
        {data.map((template) => (
          <TableRow key={template.id} onClick={() => {
            setOpenTemplate(template)
          }}>
            <TableCell>{template.id}</TableCell>
            <TableCell>{template.template}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
    <Sheet open={openTemplate != undefined} onOpenChange={open => {
      !open && setOpenTemplate(undefined)
    }}>
      <SheetContent style={{minWidth: "40%", overflowY: "auto"}} className="dark">
        <SheetHeader>
          <SheetTitle>Edit Template:</SheetTitle>
          <SheetDescription>Edit a Template</SheetDescription>
        </SheetHeader>
        {openTemplate ? <TemplateForm template={openTemplate}
                                      onDelete={(id) => sendData("/template/delete/" + id, "Deleted Template successfully!", {method: "DELETE"})}
                                      onSubmit={template => sendData("/template/save", "Template saved successfully!", {
                                      method: "POST",
                                      body: JSON.stringify(template)
                                    })}/> : ""}
      </SheetContent>
    </Sheet>
  </div>
}