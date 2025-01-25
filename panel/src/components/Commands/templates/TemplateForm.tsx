import {Template} from "./Template.ts";
import {useForm} from "react-hook-form";
import VLabel from "../../../common/VerticalLabel/VLabel.tsx";
import {Input} from "../../../../@shadcn/components/ui/input.tsx";
import TemplateEditor from "../common/templates/TemplateEditor.tsx";
import {SheetFooter} from "../../../../@shadcn/components/ui/sheet.tsx";
import {Button} from "../../../../@shadcn/components/ui/button.tsx";

export interface TemplateFormProps {
  template: Template,
  onSubmit: (template: Template) => void
  onDelete: (templateId: string) => void
}

export default function TemplateForm({template, onSubmit, onDelete}: TemplateFormProps) {
  const {handleSubmit, register} = useForm<Template>({
    defaultValues: template,
  });

  function submit(template: Template) {
    onSubmit(template);
  }

  return <div className="commandPopup">
    <VLabel name="Internal Template Name/Id:">
      <Input id="templateId" type="text" {...register("id", {required: true, disabled: true})} />
    </VLabel>
    <VLabel name="Twitch Message Color:">
      <Input id="color" type="text" {...register("messageColor", {required: false})} />
    </VLabel>
    <VLabel name="Template:">
      <TemplateEditor varSchema={template.template} register={register}/>
    </VLabel>
    <SheetFooter>
      <Button variant={"destructive"} onClick={() => onDelete(template.id)}>Delete</Button>
      <Button variant={"default"} onClick={handleSubmit(submit)}>Save</Button>
    </SheetFooter>
  </div>
}