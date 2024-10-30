import "./CommandList.css"
import {CommandForm} from "./CommandEditSheet.tsx";
import {Command, CommandPermission, CooldownTypes} from "./Command.ts";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@shadcn/components/ui/table.tsx";
import {Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle} from "@shadcn/components/ui/sheet.tsx";
import {useState} from "react";
import EnabledCheckBox from "../common/EnabledCheckBox.tsx";
import IsVisibleCheckBox from "../common/IsVisibleCheckbox.tsx";
import Loader from "../../../common/LoadingSpinner/Loader.tsx";
import useData from "../../../common/useData.ts";
import {Button} from "@shadcn/components/ui/button.tsx";
import {Input} from "@shadcn/components/ui/input.tsx";
import {Search} from "lucide-react";

const emptyCommand: Command = {
  id: "",
  description: "",
  permission: CommandPermission[CommandPermission.EVERYONE],
  userCooldown: {value: 0, type: CooldownTypes.MESSAGE},
  globalCooldown: {value: 0, type: CooldownTypes.MESSAGE},
  isAutoGenerated: false,
  patterns: [{pattern: "", isEnabled: true, isVisible: false, isRegex: false}],
  template: {id: "", template: ""}
}

export default function CommandList() {
  const {data, loading, sendData} = useData<Command[]>("/commands/all", "Commands")
  const [openCommand, setOpenCommand] = useState<Command | undefined>(undefined)
  const [isEdit, setIsEdit] = useState(true)

  if (loading) {
    return <Loader/>
  }

  return <div className="commandList">
    <div className="actionBar">
      <Button className="addCommand" onClick={() => {
        setIsEdit(false);
        setOpenCommand(emptyCommand)
      }}>Create a new Command</Button>
      <div className="searchBox">
        <Input placeholder="Search for a Command"/>
        <Button><Search/></Button>
      </div>
    </div>
    <Table>
      <TableHeader>
        <TableHead className="tw-w-16">Enabled</TableHead>
        <TableHead className="tw-w-16">Visible</TableHead>
        <TableHead>Name/Id</TableHead>
        <TableHead>First Pattern</TableHead>
        <TableHead>Template</TableHead>
      </TableHeader>
      <TableBody>
        {data.map((command) => (
          <TableRow key={command.id} onClick={() => {
            setIsEdit(true);
            setOpenCommand(command)
          }}>
            <TableCell><span className="centerInColumn">
              <EnabledCheckBox checked={command.patterns[0].isEnabled} onChange={checked => {
              }}/>
            </span></TableCell>
            <TableCell><span className="centerInColumn">
              <IsVisibleCheckBox checked={command.patterns[0].isVisible} onChange={checked => {
              }}/>
            </span></TableCell>
            <TableCell className="tw-w-96">{command.id}</TableCell>
            <TableCell className="tw-w-96">{command.patterns[0].pattern}</TableCell>
            <TableCell>{command.template.template}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
    <Sheet open={openCommand != undefined} onOpenChange={open => {
      !open && setOpenCommand(undefined)
    }}>
      <SheetContent style={{minWidth: "40%", overflowY: "auto"}} className="dark">
        <SheetHeader>
          <SheetTitle>Edit Command:</SheetTitle>
          <SheetDescription>Edit a command. All empty trigger will be ignored</SheetDescription>
        </SheetHeader>
        {openCommand ? <CommandForm command={openCommand} isEdit={isEdit}
                                    onSubmit={command => sendData("/commands/save", "Command saved successfully!", {
                                      method: "POST",
                                      body: JSON.stringify(command)
                                    })} onDelete={() => {}}/> : ""}
      </SheetContent>
    </Sheet>
  </div>
}