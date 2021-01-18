package com.banglalink.toffee.ui.home

class CatchupFragment {
    /*private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(CatchupViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.parseBundle(requireArguments())
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString("title")
        activity?.title = title
    }

    override fun loadItems():LiveData<Resource<List<ChannelInfo>>> {
        return viewModel.getContent()
    }

    companion object {
        fun createInstance(
            categoryID: Int,
            subCategoryID: Int,
            subCategory: String,
            category: String,
            title: String,
            type: String
        ): CatchupFragment {
            val catchupFragment = CatchupFragment()
            val bundle = Bundle()
            bundle.putInt("category-id", categoryID)
            bundle.putInt("sub-category-id", subCategoryID)
            bundle.putString("sub-category", subCategory)
            bundle.putString("category", category)
            bundle.putString("title", title)
            bundle.putString("type", type)
            catchupFragment.arguments = bundle
            return catchupFragment
        }
    }

    fun updateInfo(
        categoryId: Int,
        subCategoryID: Int,
        subCategory: String,
        category: String,
        title: String,
        type: String
    ) {
        activity?.title = title
        binding.progressBar.visibility = View.VISIBLE
        mAdapter?.removeAll()
        viewModel.updateInfo(category, categoryId, subCategory, subCategoryID, type)
    }*/

}