const handleGenerate = async () => {
  console.log(description);
  try {
    setLoading(true);
    const response = await generateResume(description);
    console.log('Response:', response);
    
    // Update the form data with the response
    if (response.data) {
      const newData = {
        ...data,
        summary: response.data,
        personalInformation: {
          ...data.personalInformation,
          fullName: response.data.match(/Name: ([^\n]+)/)?.[1] || data.personalInformation.fullName
        }
      };
      setData(newData);
      reset(newData);
    }

    toast.success("Resume Generated Successfully!", {
      duration: 3000,
      position: "top-center",
    });
    setShowFormUI(true);
    setShowPromptInput(false);
    setShowResumeUI(false);
  } catch (error) {
    console.error('Error:', error);
    toast.error(error.response?.data?.message || "Error Generating Resume!");
  } finally {
    setLoading(false);
    setDescription("");
  }
}; 