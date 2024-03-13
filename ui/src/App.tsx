import React, {useEffect, useState} from 'react';
import FileUpload from "./FileUpload";
import RenderForm from "./RenderForm";
import ClickToAgree from "./ClickToAgree";
import MultiSigner from "./MultiSigner";

function App() {

    const [schema, setSchema] = useState<Object>()
    const [filledFormId, setFilledFormID] = useState<string>()
    const [filledFormURL, setFilledFormURL] = useState<string>()

    function resetSchema(e: any) {
    }

    useEffect(() => {
        setFilledFormURL('http://localhost:8080/api/cta/' + filledFormId)
    }, [filledFormId])

    return (
        <div className="App">
            <br/>
            <br/>
            <br/>
            <MultiSigner filledFormId={filledFormId} filledFormURL={filledFormURL}/><br/>
            <ClickToAgree filledFormURL={filledFormURL} filleFormID={filledFormId}/><br/>
            <hr/>
            <FileUpload onFileUpload={setSchema}/>
            <RenderForm schema={schema} onSubmission={setFilledFormID}/><br/>
            {/*<button onClick={resetSchema}>Reset</button>*/}
        </div>
    );
}

export default App;