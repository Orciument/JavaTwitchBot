import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import './main.css'
import {GoalEditorPane} from "./components/Goals/GoalEditor.tsx";
import Test from "./components/Test.tsx";
import {TemplatePane} from "./components/Templates/TemplatePane/TemplatePane.tsx";

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    {/*<MessagePane/>*/}
    {/*<Test/>*/}
    <TemplatePane/>
    {/*<GoalEditorPane/>*/}
  </React.StrictMode>,
)
