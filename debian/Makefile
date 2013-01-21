#
# This Makefile is responsoble for build debian package.
# 4 variables must be provided:
#   HSN2_VER=2.X
#   BUILD_NUMBER=999
#   DEBIAN_DIST=(experimental|2.X)

DEBIAN_DIST=experimental

all:    hsn2-unicorn

clean:  hsn2-unicorn-clean

PKG=hsn2-unicorn_$(HSN2_VER)-$(BUILD_NUMBER)_all
hsn2-unicorn: hsn2-unicorn-clean
		mkdir -p $(PKG)/opt/hsn2/unicorn
		mkdir -p $(PKG)/DEBIAN
		tar -zxf ../target/hsn2-unicorn-*.tar.gz -C $(PKG)/opt/hsn2/unicorn
		cp unicorn $(PKG)/opt/hsn2/unicorn/
		cp control $(PKG)/DEBIAN
		cp postinst $(PKG)/DEBIAN
		cp prerm $(PKG)/DEBIAN
		sed -i "s/{VER}/${HSN2_VER}-${BUILD_NUMBER}/" $(PKG)/DEBIAN/control
		sed -i "s/{DEBIAN_DIST}/${DEBIAN_DIST}/" $(PKG)/DEBIAN/control
		fakeroot dpkg -b $(PKG)

hsn2-unicorn-clean:
		rm -rf hsn2-unicorn_*
