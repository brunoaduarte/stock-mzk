
$(function () {
    load();
    initModal();
});

function create(name, barCode, serialNumber, sold) {
    $.post("/api/products", JSON.stringify({name: name, barCode: barCode, serialNumber: serialNumber, sold: sold}), function (response) {
    	swal("Product '" + response.name + "' added!", { icon: "success" });
        load();
    }, "json").fail(function(response) {
        swal("Error", response.responseText, "error");
    });
}

function remove(id) {
	
	swal({
		  title: "Are you sure you want to remove product '" + id + "'?",
		  text: "Once deleted, you will not be able to recover this product!",
		  icon: "warning",
		  buttons: true,
		  dangerMode: true,
		})
		.then((willDelete) => {
		  if (willDelete) {
			  
		    $.ajax({
		        method: "DELETE",
		        url: "/api/products/" + id
		    }).done(function () {
			    swal("Product " + id + " removed!", { icon: "success" });
		        load();
		    }).fail(function(response) {
		        swal("Error", response.responseText, "error");
		    });
		  }
		});
	
}

function update(id, name, barCode, serialNumber, sold) {
    $.ajax({
        method: "PUT",
        url: "/api/products/" + id,
        data: JSON.stringify({name: name, barCode: barCode, serialNumber: serialNumber, sold: sold})
    }).done(function(response) {
    	swal("Product '" + id + "' updated!", { icon: "success" });
        load();
    }).fail(function(response) {
    	swal("Error", response.responseText, "error");
    });
}

function load() {
    $("#content").children().remove();
    $.getJSON("/api/products", function (data) {
        $.each(data, function (key, val) {
            $("<tr><td>" + val.id + "</td><td>" + val.name + "</td><td>" + val.barCode + "</td><td>" + val.serialNumber + "</td><td>" + (val.sold ? "✔️" : "") +
                    "<td>" +
                    "<button data-action='edit' class='btn btn-primary btn-sm product-edit' " +
                    "data-toggle='modal' " +
                    "data-target='#productModal' " +
                    "data-name='" + val.name + "' " +
                    "data-bar-code='" + val.barCode + "' " +
                    "data-serial-number='" + val.serialNumber + "' " +
                    "data-sold='" + val.sold + "' " +
                    "data-id='" + val.id + "'>" +
                    "<span class='glyphicon glyphicon-pencil'></span>" +
                    "</button>" +
                    "&nbsp;" +
                    "<button class='btn btn-danger btn-sm product-delete' data-id='" + val.id + "'>" +
                    "   <span class='glyphicon glyphicon-minus'></span>" +
                    "</button>" +
                    "</td>" +
                    "</tr>").appendTo("#content");
        });
        initCallbacks();
    });
}

function initCallbacks() {
    $(".product-delete").unbind().click(function() {
       var id = $(this).data("id");
       remove(id);
    });
}

function initModal() {
    $("#productModal").on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var action = button.data('action');
        var id = button.data('id');
        var productAction = $("#productAction");
        productAction.unbind();
        var modal = $(this);
        if (action === "add") {
            modal.find('.modal-title').text("Add a product");
            modal.find('#product-name').val("");
            modal.find('#product-bar-code').val("");
            modal.find('#product-serial-number').val("");
            modal.find('#product-sold').prop("checked", false);
            productAction.click(function () {
                create($("#product-name").val(), Number($("#product-bar-code").val()), Number($("#product-serial-number").val()), $("#product-sold").prop("checked"));
                $('#productModal').modal('toggle');
            });
        } else {
            modal.find('.modal-title').text("Edit a product");
            modal.find('#product-name').val(button.data("name"));
            modal.find('#product-bar-code').val(button.data("barCode"));
            modal.find('#product-serial-number').val(button.data("serialNumber"));
            modal.find('#product-sold').prop("checked", button.data("sold"));
            productAction.click(function () {
                update(id, $("#product-name").val(), Number($("#product-bar-code").val()), Number($("#product-serial-number").val()), $("#product-sold").prop("checked"));
                $('#productModal').modal('toggle');
            });
        }
    })
}
