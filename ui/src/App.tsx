import React, {useState} from 'react';
import FileUpload from "./FileUpload";
import RenderForm from "./RenderForm";

function App() {

    const [schema, setSchema] = useState<Object>()

    function resetSchema(e: any) {
    }

    return (
        <div className="App">
            <FileUpload onFileUpload={setSchema}/>
            <RenderForm schema={schema}/>
            {/*<button onClick={resetSchema}>Reset</button>*/}
        </div>
    );
}

export default App;