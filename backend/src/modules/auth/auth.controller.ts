import { Body, Controller, Post } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { AuthService } from './auth.service';
import { RegisterDto } from './register.dto';

@Controller('auth')
export class AuthController {
  constructor(
    private readonly prisma: PrismaService,
    private readonly auth: AuthService,
  ) {}

  @Post('register')
  async register(@Body() dto: RegisterDto) {
    const user = await this.prisma.user.upsert({
      where: { phone: dto.phone },
      create: {
        phone: dto.phone,
        name: dto.name,
        settings: { create: {} },
      },
      update: { name: dto.name },
    });

    const device = await this.prisma.device.upsert({
      where: {
        userId_androidDeviceId: {
          userId: user.id,
          androidDeviceId: dto.androidDeviceId,
        },
      },
      create: {
        userId: user.id,
        androidDeviceId: dto.androidDeviceId,
      },
      update: { lastSeenAt: new Date() },
    });

    const accessToken = this.auth.signToken(user.id, user.phone);

    return {
      userId: user.id,
      deviceId: device.id,
      accessToken,
      name: user.name,
      phone: user.phone,
    };
  }
}
